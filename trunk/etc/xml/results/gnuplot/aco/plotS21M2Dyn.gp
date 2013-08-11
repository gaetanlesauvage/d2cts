set term pdf enhanced size 21cm,29.7cm 
set output 'traceS21M2Dyn.pdf' 
set xlabel "Algorithm"
set ylabel "Distance (m)"
set timefmt "%H:%M:%S"
set grid
set rmargin 1 
set lmargin 1 
set tmargin 1 
set bmargin 1
set size 1,1
set origin 0,0
set multiplot layout 2,1 columnsfirst scale 0.8,0.8

set ytics border
set yrange [5500:8000]

set style line 1 linewidth 2 pt 1
set style line 2 linewidth 2 pt 2
set style line 3 linewidth 2 pt 3 
set style line 4 linewidth 2 pt 4 
set style line 5 linewidth 2 pt 5
set style line 6 linewidth 2 pt 6
set style line 7 linewidth 2 pt 7


set title "S=21 M=2 Distances"
plot "dataS21M2Dyn.dat" using 16:xticlabel(1) with lp ls 1 title "distance_{0-0}", "dataS21M2Dyn.dat" using 17:xticlabel(1) w lp ls 2 title "distance_{0.5-0.33}",  "dataS21M2Dyn.dat" using 18:xticlabel(1) w lp ls 3 title "distance_{0.5-0.66}",  "dataS21M2Dyn.dat" using 19:xticlabel(1) w lp ls 4 title "distance_{0.5-1}",  "dataS21M2Dyn.dat" using 20:xticlabel(1) w lp ls 5 title "distance_{1-0.33}",  "dataS21M2Dyn.dat" using 21:xticlabel(1) w lp ls 6 title "distance_{1-0.66}", "dataS21M2Dyn.dat" using 22:xticlabel(1) w lp ls 7 title "distance_{1-1}"

set ydata time
set yrange ['00:00:00':'01:25:00']
set ylabel "Overspent Time (hh:mm:ss)"
set format y "%H:%M:%S"
set ytics border
set title "S=21 M=2 Overspent Time"
plot "dataS21M2Dyn.dat" using 23:xticlabel(1) axes x1y1 w lp ls 1 title "0,0", "dataS21M2Dyn.dat" using 24:xticlabel(1) axes x1y1 w lp ls 2 title "0.5,0.33", "dataS21M2Dyn.dat" using 25:xticlabel(1) axes x1y1 w lp ls 3 title "0.5,0.66", "dataS21M2Dyn.dat" using 26:xticlabel(1) axes x1y1 w lp ls 4 title "0.5,1",  "dataS21M2Dyn.dat" using 27:xticlabel(1) axes x1y1 w lp ls 5 title "1,0.33", "dataS21M2Dyn.dat" using 28:xticlabel(1) axes x1y1 w lp ls 6 title "1,0.66", "dataS21M2Dyn.dat" using 29:xticlabel(1) axes x1y1 w lp ls 7 title "1,1"

unset multiplot