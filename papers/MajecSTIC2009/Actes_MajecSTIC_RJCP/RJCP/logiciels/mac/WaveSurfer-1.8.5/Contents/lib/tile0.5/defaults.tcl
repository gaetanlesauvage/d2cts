#
# $Id: defaults.tcl,v 1.23 2004/10/18 02:05:36 jenglish Exp $
#
# Tile widget set: Default theme
#

namespace eval tile {

    package provide tile::theme::default $::tile::version

    variable colors
    array set colors {
	-frame		"#d9d9d9"
	-activebg	"#ececec"
	-selectbg	"#4a6984"
	-selectfg	"#ffffff"
	-darker 	"#c3c3c3"
	-disabledfg	"#a3a3a3"
	-indicator	"#4a6984"
    }

    style theme settings default {

	style default "." \
	    -borderwidth 	1 \
	    -background 	$colors(-frame) \
	    -foreground 	black \
	    -troughcolor 	$colors(-darker) \
	    -font 		TkDefaultFont \
	    -selectborderwidth	1 \
	    -selectbackground	$colors(-selectbg) \
	    -selectforeground	$colors(-selectfg) \
	    -insertwidth 	2 \
	    -indicatordiameter	10 \
	    ;

	style map "." -background \
	    [list disabled $colors(-frame)  active $colors(-activebg)]
	style map "." -foreground \
		[list disabled $colors(-disabledfg)]

	style default TButton \
	    -padding "3 3" -width -9 -relief raised -shiftrelief 1
	style map TButton -relief [list {!disabled pressed} sunken] 

	foreach class {TCheckbutton TRadiobutton} {
	    style default $class \
	    	-indicatorcolor "#ffffff" -indicatorrelief sunken 
	    style map $class -indicatorcolor [list \
		pressed 	$colors(-activebg) \
		selected	$colors(-indicator) \
	    ]
	}

	style default TMenubutton -relief raised -padding "10 3"

	style default TEntry -relief sunken -fieldbackground white -padding 1
	style map TEntry -fieldbackground \
	    [list readonly $colors(-frame) disabled $colors(-frame)]

	style default TCombobox -arrowsize 12
	style map TCombobox -fieldbackground \
		[list readonly $colors(-frame) disabled $colors(-frame)]

	style default TScrollbar -width 12 -arrowsize 12
	#style map TScrollbar -relief {{pressed !disabled} sunken}
	style map TScrollbar -arrowcolor [list disabled $colors(-disabledfg)]

	style default TScale -sliderrelief raised
	#style map TScale -sliderrelief {{pressed !disabled} sunken}

	style default TProgress -background $colors(-selectbg)

	style default TNotebook.Tab -padding {4 2} -background $colors(-darker)
	style map TNotebook.Tab -background \
	    [list selected $colors(-frame) active $colors(-activebg)]

	#
	# Toolbar buttons:
	#
	style layout Toolbutton {
	    Toolbutton.border -children {
		Toolbutton.padding -children {
		    Toolbutton.label
		}
	    }
	}

	style default Toolbutton -padding 2 -relief flat
	style map Toolbutton -relief \
	    {disabled flat selected sunken pressed sunken active raised}
	style map Toolbutton -background \
	    [list pressed $colors(-darker)  active $colors(-activebg)]
    }
}
