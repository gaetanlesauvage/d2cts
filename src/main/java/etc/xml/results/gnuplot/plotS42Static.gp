set term pdf enhanced size 21cm,29.7cm 
set output 'traceS42Static.pdf' 
set xlabel "Algorithm"
set ylabel "Distance (m)"
set y2label "Overspent Time (hh:mm:ss)"
set grid
set y2data time
set timefmt "%H:%M:%S"
set y2range ['00:00:00':'00:35:00']
set format y2 "%H:%M:%S"
set y2tics border
set ytics border
set yrange [11000:15000]
set rmargin 1 
set lmargin 1 
set tmargin 1 
set bmargin 1
set size 1,1
set origin 0,0
set multiplot layout 3,1 columnsfirst scale 0.8,0.8
set style line 1 linewidth 2
set style line 2 linewidth 2
#set origin 0,0
#set size square 0.33,1
set title "Results S=42 M=2 STATIC"
plot "dataS42Static.dat" using 2:xticlabel(1) with line ls 1 title "distance", "dataS42Static.dat" using 5:xticlabel(1) axes x1y2 with line ls 2 title "overspent time"

#set origin 0.33,0
#set size 0.33,1
set title "Results S=42 M=3 STATIC"
plot "dataS42Static.dat" using 3:xticlabel(1) with line ls 1 title "distance", "dataS42Static.dat" using 6:xticlabel(1) axes x1y2 with line ls 2 title "overspent time"


#set origin 0.66,0
#set size 0.33,1
set title "Results S=42 M=4 STATIC (Branch & Bound NA)"
plot "dataS42Static.dat" using 4:xticlabel(1) with line ls 1 title "distance", "dataS42Static.dat" using 7:xticlabel(1) axes x1y2 with line ls 2 title "overspent time"

unset multiplot