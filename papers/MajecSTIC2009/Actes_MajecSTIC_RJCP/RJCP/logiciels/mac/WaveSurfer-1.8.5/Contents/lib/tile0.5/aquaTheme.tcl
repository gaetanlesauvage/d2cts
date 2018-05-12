#
# $Id: aquaTheme.tcl,v 1.8 2004/11/26 21:25:21 jenglish Exp $
#
# Tile widget set: Aqua theme (OSX native look and feel)
#

namespace eval tile {

    style theme settings aqua {

	style default . \
	    -font System \
	    -background White \
	    -foreground Black \
	    -selectbackground SystemHighlight \
	    -selectforeground SystemHighlightText \
	    -selectborderwidth 0 \
	    -insertwidth 1 \
	    ;
	style map . \
	    -foreground [list  disabled "#a3a3a3"  background "#a3a3a3"] \
	    -selectbackground [list background "#c3c3c3"  !focus "#c3c3c3"] \
	    -selectforeground [list background "#a3a3a3"  !focus "#000000"] \
	    ;

	style default TButton -padding {0 2} -width -6
	style default Toolbutton -padding 4
	style default TNotebook.Tab -padding {10 2 10 2}

	# A hack:
	# Aqua doesn't use underlines to indicate mnemonics;
	# in addition, the Mac doesn't seem to even have an 'Alt' key,
	# so the Alt-Keypress bindings set up by keynav.tcl never fire.
	# So: Override widget -underline options here ('style map'
	# settings take precedence):
	#
	style map . -underline {{} -1}

	# Modify the the default Labelframe layout to use generic text element
	# instead of Labelframe.text; the latter erases the window background
	# (@@@ this still isn't right... want to fill with background pattern)

	style layout TLabelframe {
	    Labelframe.border
	    text
	}

    }
}
