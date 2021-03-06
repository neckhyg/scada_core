
package com.serotonin.m2m2.web.dwr.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.dwrp.SimpleOutboundVariable;
import org.directwebremoting.extend.Converter;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.NamedConverter;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;
import org.directwebremoting.extend.TypeHintContext;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Messages;

public class BlabberConverterManager implements ConverterManager {
    private static final Log log = LogFactory.getLog(BlabberConverterManager.class);
    private final Map<TypeHintContext, Class<?>> extraTypeInfoMap = Collections
            .synchronizedMap(new HashMap<TypeHintContext, Class<?>>());
    private final Map<String, Class<Converter>> converterTypes = new HashMap<String, Class<Converter>>();
    private final Map<String, Converter> converters = Collections.synchronizedMap(new HashMap<String, Converter>());
    private static List<String> ignore = Arrays.asList(new String[] { "converter", "match" });

    public void addConverterType(String id, String className) {
        if (!LocalUtil.isJavaIdentifier(id)) {
            log.error("Illegal identifier: '" + id + "'");
            return;
        }

        @SuppressWarnings("unchecked")
        Class<Converter> clazz = LocalUtil.classForName(id, className, Converter.class);
        if (clazz != null) {
            log.debug("- adding converter type: " + id + " = " + clazz.getName());
            converterTypes.put(id, clazz);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#addConverter(java.lang.String, java.lang.String, java.util.Map)
     */
    public void addConverter(String match, String type, @SuppressWarnings("rawtypes") Map params)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException {
        Class<Converter> clazz = converterTypes.get(type);
        if (clazz == null) {
            log.info("Probably not an issue: " + match + " is not available so the " + type
                    + " converter will not load. This is only an problem if you wanted to use it.");
            return;
        }

        Converter converter = clazz.newInstance();
        converter.setConverterManager(this);

        LocalUtil.setParams(converter, params, ignore);

        // add the converter for the specified match
        addConverter(match, converter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#addConverter(java.lang.String, org.directwebremoting.Converter)
     */
    public void addConverter(String match, Converter converter) throws IllegalArgumentException {
        // Check that we don't have this one already
        Converter other = converters.get(match);
        if (other != null) {
            log.warn("Clash of converters for " + match + ". Using " + converter.getClass().getName() + " in place of "
                    + other.getClass().getName());
        }

        log.debug("- adding converter: " + LocalUtil.getShortClassName(converter.getClass()) + " for " + match);
        converters.put(match, converter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#getConverterMatchStrings()
     */
    @SuppressWarnings("rawtypes")
    public Collection getConverterMatchStrings() {
        return Collections.unmodifiableSet(converters.keySet());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#getConverterByMatchString(java.lang.String)
     */
    public Converter getConverterByMatchString(String match) {
        return converters.get(match);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#isConvertable(java.lang.Class)
     */
    public boolean isConvertable(@SuppressWarnings("rawtypes") Class paramType) {
        return getConverter(paramType) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#convertInbound(java.lang.Class,
     * org.directwebremoting.InboundVariable, org.directwebremoting.InboundContext,
     * org.directwebremoting.TypeHintContext)
     */
    public Object convertInbound(@SuppressWarnings("rawtypes") Class paramType, InboundVariable iv,
            InboundContext inctx, TypeHintContext incc) throws MarshallException {
        Object converted = inctx.getConverted(iv, paramType);
        if (converted == null) {
            // Was the inbound variable marshalled as an Object in the client
            // (could mean that this is an instance of one of our generated
            // JavaScript classes)
            Converter converter = getNamedConverter(paramType, iv.getType());

            // Fall back to the standard way of locating a converter if we
            // didn't find anything above
            if (converter == null) {
                converter = getConverter(paramType);
            }

            if (converter == null) {
                throw new MarshallException(paramType, Messages.getString("DefaultConverterManager.MissingConverter",
                        paramType));
            }

            // We only think about doing a null conversion ourselves once we are
            // sure that there is a converter available. This prevents hackers
            // from passing null to things they are not allowed to convert
            if (iv.isNull()) {
                return null;
            }

            inctx.pushContext(incc);
            converted = converter.convertInbound(paramType, iv, inctx);
            inctx.popContext();
        }

        return converted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#convertOutbound(java.lang.Object,
     * org.directwebremoting.OutboundContext)
     */
    public OutboundVariable convertOutbound(Object object, OutboundContext outctx) throws MarshallException {
        if (object == null) {
            return new SimpleOutboundVariable("null", outctx, true);
        }

        // Check to see if we have done this one already
        OutboundVariable ov = outctx.get(object);
        if (ov != null) {
            // So the object as been converted already, we just need to refer to it.
            return ov.getReferenceVariable();
        }

        // So we will have to do the conversion
        Converter converter = getConverter(object);
        if (converter == null) {
            log.error(Messages.getString("DefaultConverterManager.MissingConverter", object.getClass().getName()));
            return new SimpleOutboundVariable("null", outctx, true);
        }

        return converter.convertOutbound(object, outctx);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#setExtraTypeInfo(org.directwebremoting.TypeHintContext,
     * java.lang.Class)
     */
    public void setExtraTypeInfo(TypeHintContext thc, @SuppressWarnings("rawtypes") Class type) {
        extraTypeInfoMap.put(thc, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#getExtraTypeInfo(org.directwebremoting.TypeHintContext)
     */
    @SuppressWarnings("rawtypes")
    public Class getExtraTypeInfo(TypeHintContext thc) {
        Class<?> type = extraTypeInfoMap.get(thc);
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.directwebremoting.ConverterManager#setConverters(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void setConverters(@SuppressWarnings("rawtypes") Map converters) {
        synchronized (this.converters) {
            this.converters.clear();
            this.converters.putAll(converters);
        }
    }

    /**
     * Like <code>getConverter(object.getClass());</code> except that since the object can be null we check for that
     * fist and then do a lookup against the <code>Void.TYPE</code> converter
     * 
     * @param object
     *            The object to find a converter for
     * @return The converter for the given type
     */
    private Converter getConverter(Object object) {
        if (object == null) {
            return getConverter(Void.TYPE);
        }

        return getConverter(object.getClass());
    }

    /**
     * When we are using typed Javascript names we sometimes want to get a specially named converter
     * 
     * @param paramType
     *            The class that we are converting to
     * @param type
     *            The type name as passed in from the client
     * @return The Converter that matches this request (if any)
     * @throws MarshallException
     */
    protected Converter getNamedConverter(Class<?> paramType, String type) throws MarshallException {
        if (type.startsWith("Object_")) {
            // Extract the JavaScript classname from the inbound type
            String javascriptClassName = type.substring("Object_".length());

            // TODO do we need to check the incoming param type for annotations here?

            // Locate a converter for this JavaScript classname
            synchronized (converters) {
                Iterator<Map.Entry<String, Converter>> it = converters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Converter> entry = it.next();
                    String match = entry.getKey();
                    Converter conv = entry.getValue();

                    // JavaScript mapping is only applicable for compound converters
                    if (conv instanceof NamedConverter) {
                        NamedConverter boConv = (NamedConverter) conv;
                        if (boConv.getJavascript() != null && boConv.getJavascript().equals(javascriptClassName)) {
                            // We found a potential converter! But is the
                            // converter's Java class compatible with the
                            // parameter type?
                            try {
                                Class<?> inboundClass = LocalUtil.classForName(match);
                                if (paramType.isAssignableFrom(inboundClass)) {
                                    // Hack: We also want to make sure that the
                                    // converter creates its object based on the
                                    // inbound class instead of the parameter
                                    // type, and we have to use the other ref
                                    // for this:
                                    boConv.setInstanceType(inboundClass);
                                    return boConv;
                                }
                            }
                            catch (ClassNotFoundException ex) {
                                throw new MarshallException(paramType, ex);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * @param paramType
     *            The type to find a converter for
     * @return The converter for the given type, or null if one can't be found
     */
    public Converter getConverter(Class<?> paramType) {
        // Can we find a converter assignable to paramType in the HashMap?
        Converter converter = getConverterAssignableFrom(paramType);
        if (converter != null) {
            return converter;
        }

        String lookup = paramType.getName();

        // Before we start trying for a match on package parts we check for
        // dynamic proxies
        if (lookup.startsWith("$Proxy")) {
            converter = converters.get("$Proxy*");
            if (converter != null) {
                return converter;
            }
        }

        while (true) {
            // Can we find a converter using wildcards?
            converter = converters.get(lookup + ".*");
            if (converter != null) {
                return converter;
            }

            // Arrays can have wildcards like [L* so we don't require a '.'
            converter = converters.get(lookup + '*');
            if (converter != null) {
                return converter;
            }

            // Give up if the name is now empty
            if (lookup.length() == 0) {
                break;
            }

            // Strip of the component after the last .
            int lastdot = lookup.lastIndexOf('.');
            if (lastdot != -1) {
                lookup = lookup.substring(0, lastdot);
            }
            else {
                int arrayMarkers = 0;
                while (lookup.charAt(arrayMarkers) == '[') {
                    arrayMarkers++;
                }

                if (arrayMarkers == 0) {
                    // so we are out of dots and out of array markers
                    // bail out.
                    break;
                }

                // We want to keep the type marker too
                lookup = lookup.substring(arrayMarkers - 1, arrayMarkers + 1);

                // Now can we find it?
                converter = converters.get(lookup);
                if (converter != null) {
                    return converter;
                }
            }
        }

        return null;
    }

    /**
     * @param paramType
     *            The type to find a converter for
     * @return The converter assignable for the given type, or null if one can't be found
     */
    private Converter getConverterAssignableFrom(Class<?> paramType) {
        if (paramType == null) {
            return null;
        }

        String lookup = paramType.getName();

        // Can we find the converter for paramType in the converters HashMap?
        Converter converter = null;
        synchronized (this.converters) {
            converter = converters.get(lookup);
            if (converter != null)
                return converter;

            // Lookup all of the interfaces of this class for a match
            Class<?>[] interfaces = paramType.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                converter = getConverterAssignableFrom(interfaces[i]);
                if (converter != null) {
                    converters.put(lookup, converter);
                    return converter;
                }
            }

            // Let's search it in paramType superClass
            converter = getConverterAssignableFrom(paramType.getSuperclass());
            if (converter != null) {
                converters.put(lookup, converter);
            }
        }

        return converter;
    }

    /**
     * @param paramType
     *            The type to find a converter for
     * @return The converter assignable for the given type, or null if one can't be found
     */
    public Converter getConverterAssignableFromNoAdd(Class<?> paramType) {
        String lookup = paramType.getName();

        Converter converter = null;
        synchronized (this.converters) {
            converter = converters.get(lookup);
            if (converter != null)
                return converter;

            // Lookup all of the interfaces of this class for a match
            Class<?>[] interfaces = paramType.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                converter = getConverterAssignableFrom(interfaces[i]);
                if (converter != null)
                    return converter;
            }

            // Let's search it in paramType superClass
            converter = getConverterAssignableFrom(paramType.getSuperclass());
        }

        return converter;
    }
}
