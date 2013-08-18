if {[catch {package require Tcl 8.4}]} return
package ifneeded tile 0.6  [list load [file join $dir libtile0.6.dylib] tile]
