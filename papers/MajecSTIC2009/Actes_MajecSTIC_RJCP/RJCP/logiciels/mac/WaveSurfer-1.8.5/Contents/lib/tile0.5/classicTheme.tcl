#
# $Id: classicTheme.tcl,v 1.6 2004/11/09 16:23:18 jenglish Exp $
#
# Tile widget set: Classic theme.
# Implements the classic Tk Motif-like look and feel.
#

namespace eval tile::theme::classic {

    font create TkClassicDefaultFont -family Helvetica -weight bold -size -12

    array set colors {
	-frame		"#d9d9d9"
	-activebg	"#ececec"
	-troughbg	"#c3c3c3"
	-selectbg	"#c3c3c3"
	-selectfg	"#000000"
	-disabledfg	"#a3a3a3"
	-indicator	"#b03060"
    }

    style theme settings classic {
	style default "." \
	    -font		TkClassicDefaultFont \
	    -background		$colors(-frame) \
	    -foreground		black \
	    -selectbackground	$colors(-selectbg) \
	    -selectforeground	$colors(-selectfg) \
	    -troughcolor	$colors(-troughbg) \
	    -indicatorcolor	$colors(-frame) \
	    -highlightcolor	$colors(-frame) \
	    -highlightthickness	1 \
	    -selectborderwidth	1 \
	    -insertwidth	2 \
	    ;

	style map "." -background \
	    [list disabled $colors(-frame) active $colors(-activebg)]
	style map "." -foreground \
	    [list disabled $colors(-disabledfg)]

	style map "." -highlightcolor [list focus black]

	style default TButton -padding "3m 1m" -relief raised -shiftrelief 1
	style map TButton -relief [list {!disabled pressed} sunken]

	foreach class {TCheckbutton TRadiobutton} {
	    style default $class -indicatorrelief raised
	    style map $class -indicatorrelief {selected sunken pressed sunken}
	    style map $class -indicatorcolor [list \
		    pressed	$colors(-frame) \
		    selected	$colors(-indicator) \
	    ]
	}

	style default TMenubutton -relief raised -padding "3m 1m"

	style default TEntry -relief sunken -padding 1 -font TkTextFont
	style map TEntry -fieldbackground \
		[list readonly $colors(-frame) disabled $colors(-frame)]
	style map TCombobox -fieldbackground \
		[list readonly $colors(-frame) disabled $colors(-frame)]

	style default TScrollbar -relief raised
	style map TScrollbar -relief {{pressed !disabled} sunken}

	style default TScale -sliderrelief raised
	style map TScale -sliderrelief {{pressed !disabled} sunken}

	style default TProgress -background SteelBlue
	style default TNotebook.Tab \
	    -padding {3m 1m} \
	    -background $colors(-troughbg)
	style map TNotebook.Tab -background [list selected $colors(-frame)]

	#
	# Toolbar buttons:
	#
	style default Toolbutton -padding 2 -relief flat -shiftrelief 2
	style map Toolbutton -relief \
	    {disabled flat selected sunken pressed sunken active raised}
	style map Toolbutton -background \
	    [list pressed $colors(-troughbg)  active $colors(-activebg)]
    }
}
