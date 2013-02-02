/* Chinese initialisation for the jQuery UI time picker plugin. */
/* Written by Cloudream (cloudream@gmail.com). */
jQuery(function($){
	
	$.timepicker.regional['zh-CN'] = { 
			currentText: '当前',
			closeText: '完成',
			ampm: false,
			amNames: ['上午', 'A'],
			pmNames: ['下午', 'P'],
			timeFormat: 'hh:mm tt',
			timeSuffix: '',
			timeOnlyTitle: '选择时间',
			timeText: '时间',
			hourText: '小时',
			minuteText: '分钟',
			secondText: '秒',
			millisecText: '毫秒',
			timezoneText: '时区'
		};	        
	 $.timepicker.setDefaults($.timepicker.regional['zh-CN']);

});
