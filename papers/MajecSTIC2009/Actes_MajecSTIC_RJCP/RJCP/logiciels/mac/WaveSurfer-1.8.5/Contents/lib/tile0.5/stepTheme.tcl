#
# $Id: stepTheme.tcl,v 1.20 2004/11/29 21:40:00 jenglish Exp $
#
# Appearance settings for "Step" theme.
#

namespace eval tile::theme::step {

    array set colors {
	-frame		"#a0a0a0"
	-activebg	"#aeb2c3"
	-selectbg	"#fdcd00"
	-disabledfg	"#808080"
	-trough		"#c3c3c3"
    }

    style theme settings step {

	style default "." \
	    -background 	$colors(-frame) \
	    -foreground 	black \
	    -troughcolor 	$colors(-trough) \
	    -selectbackground 	$colors(-selectbg) \
	    -font  		TkDefaultFont \
	    ;

	style map "." \
	    -foreground [list disabled $colors(-disabledfg)] \
	    -background [list {active !disabled} $colors(-activebg)] \
	    ;

	style default TButton -padding "3m 0" -relief raised -shiftrelief 1
	style map TButton -relief {
	    {pressed !disabled} 	sunken
	    {active !disabled} 	raised
	}

	style default TCheckbutton \
	    -indicatorrelief groove \
	    -indicatorcolor $colors(-frame) \
	    -borderwidth 2
	style map TCheckbutton \
	    -indicatorrelief {pressed ridge} \
	    -indicatorcolor  [list active $colors(-activebg)]

	style default TRadiobutton -indicatorcolor $colors(-frame)
	style map TRadiobutton -indicatorrelief [list pressed sunken] 

	style default TMenubutton -padding "3 3" -relief raised

	style default TEntry \
	    -relief sunken -borderwidth 1 -padding 1 -font TkTextFont

	style map TScrollbar -relief { pressed sunken  {} raised }
	style map TScrollbar -background \
	    [list  disabled $colors(-frame)  active $colors(-activebg)] ;

	style default TProgress \
	    -background $colors(-activebg) -borderwidth 1

	style default TScale \
	    -borderwidth 1 -groovewidth 4 -troughrelief sunken

	style default TNotebook.Tab -padding {10 3} -background $colors(-frame)
	style map TNotebook.Tab \
	    -padding [list selected {12 6 12 3}] \
	    -background [list \
	    	selected $colors(-frame) \
		active $colors(-activebg)] \
	    ;
    }
}
