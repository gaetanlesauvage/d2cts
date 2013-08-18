%% This is file `phdlasl.cls', generated
%% on <1996/9/16> with the docstrip utility (2.2i).
%%
%% The original source files were:
%%
%% thesecal.dtx  (with options: `package') + certains morceaux de thloria.cls
%%
%% \CharacterTable
%%  {Upper-case    \A\B\C\D\E\F\G\H\I\J\K\L\M\N\O\P\Q\R\S\T\U\V\W\X\Y\Z
%%   Lower-case    \a\b\c\d\e\f\g\h\i\j\k\l\m\n\o\p\q\r\s\t\u\v\w\x\y\z
%%   Digits        \0\1\2\3\4\5\6\7\8\9
%%   Exclamation   \!     Double quote  \"     Hash (number) \#
%%   Dollar        \$     Percent       \%     Ampersand     \&
%%   Acute accent  \'     Left paren    \(     Right paren   \)
%%   Asterisk      \*     Plus          \+     Comma         \,
%%   Minus         \-     Point         \.     Solidus       \/
%%   Colon         \:     Semicolon     \;     Less than     \<
%%   Equals        \=     Greater than  \>     Question mark \?
%%   Commercial at \@     Left bracket  \[     Backslash     \\
%%   Right bracket \]     Circumflex    \^     Underscore    \_
%%   Grave accent  \`     Left brace    \{     Vertical bar  \|
%%   Right brace   \}     Tilde         \~}
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\NeedsTeXFormat{LaTeX2e}[95/01/31]
\ProvidesClass{phdlasl}[95/07/01 v2.0]
\typeout{Classe ``THESE'' du GREEN, Bon Courache et Bonne Chance ...}
\newcommand{\@ptsize}{}
\newif\if@restonecol
\newif\if@openright
\newif\if@openbib
\newif\if@draft
\newif\if@draftindex
\newif\if@draftbib
\newif\if@nodate
\newif\if@frenchdp
\@openbibfalse
\@draftfalse
\@draftindexfalse
\@draftbibfalse
\@nodatefalse
\@frenchdpfalse
\DeclareOption{a4paper}
   {\setlength\paperheight {297mm}%
    \setlength\paperwidth  {210mm}}
\DeclareOption{a5paper}
   {\setlength\paperheight {210mm}%
    \setlength\paperwidth  {148mm}}
\DeclareOption{b5paper}
   {\setlength\paperheight {250mm}%
    \setlength\paperwidth  {176mm}}
\DeclareOption{letterpaper}
   {\setlength\paperheight {11in}%
    \setlength\paperwidth  {8.5in}}
\DeclareOption{legalpaper}
   {\setlength\paperheight {14in}%
    \setlength\paperwidth  {8.5in}}
\DeclareOption{executivepaper}
   {\setlength\paperheight {10.5in}%
    \setlength\paperwidth  {7.25in}}
\DeclareOption{landscape}
   {\setlength\@tempdima   {\paperheight}%
    \setlength\paperheight {\paperwidth}%
    \setlength\paperwidth  {\@tempdima}}
\DeclareOption{10pt}{\renewcommand{\@ptsize}{0}}
\DeclareOption{11pt}{\renewcommand{\@ptsize}{1}}
\DeclareOption{12pt}{\renewcommand{\@ptsize}{2}}
\DeclareOption{oneside}{\@twosidefalse \@mparswitchfalse}
\DeclareOption{twoside}{\@twosidetrue  \@mparswitchtrue}
\DeclareOption{onecolumn}{\@twocolumnfalse}
\DeclareOption{twocolumn}{\@twocolumntrue}
\DeclareOption{draft}{\setlength\overfullrule{5pt} \@drafttrue}
\DeclareOption{draftindex}{\@draftindextrue}
\DeclareOption{draftbib}{\@draftbibtrue}
\DeclareOption{final}{\setlength\overfullrule{0pt} \@draftfalse
  \@draftindexfalse \@draftbibfalse}
\DeclareOption{nodate}{\@nodatetrue}
\DeclareOption{openright}{\@openrighttrue}
\DeclareOption{openany}{\@openrightfalse}
\DeclareOption{leqno}{\input{leqno.clo}}
\DeclareOption{fleqn}{\input{fleqn.clo}}
\DeclareOption{openbib}{\@openbibtrue}
\DeclareOption{frenchdp}{\@frenchdptrue}
\ExecuteOptions{a4paper,11pt,twoside,onecolumn,final,openright}
\ProcessOptions
\input{size1\@ptsize.clo}
\setlength\lineskip{1\p@}
\setlength\normallineskip{1\p@}
\renewcommand{\baselinestretch}{1.5} % Interligne du document 
\setlength\parskip{0\p@ \@plus \p@}
\@lowpenalty   51
\@medpenalty  151
\@highpenalty 301
\setcounter{topnumber}{2}
\renewcommand{\topfraction}{.7}
\setcounter{bottomnumber}{1}
\renewcommand{\bottomfraction}{.3}
\setcounter{totalnumber}{3}
\renewcommand{\textfraction}{.2}
\renewcommand{\floatpagefraction}{.5}
\setcounter{dbltopnumber}{2}
\renewcommand{\dbltopfraction}{.7}
\renewcommand{\dblfloatpagefraction}{.5}
\newcommand{\Grule}{\hrule height 0.3pt}
\if@twoside
\def\ps@headings{%
  \if@draft
    \def\@oddfoot{\hfill{\tiny �preuve du \today}}
    \def\@evenfoot{\hfill{\tiny �preuve du \today}}
  \else
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
  \fi
  \def\@evenhead{%
  $\stackrel{\makebox[\textwidth]{\slshape\thepage\hfil\hfill\leftmark}}%
    {\hrulefill}$}
  \def\@oddhead{%
  $\stackrel{\makebox[\textwidth]{\slshape\rightmark\hfil\hfill\thepage}}%
    {\hrulefill}$}
  \let\@mkboth\markboth
  \def\chaptermark##1{%
%    \markboth {\lowercase{%
     \markboth {{
        \ifnum \c@secnumdepth >\m@ne
        \@chapapp\ \thechapter~: \ %
        \fi
        ##1}}{}}%
  \def\sectionmark##1{%
    \markright {%
        \ifnum \c@secnumdepth >\z@
        \thesection~: \ %
        \fi
        ##1}}}
\else
\def\ps@headings{%
  \if@draft
    \def\@oddfoot{\hfill{\tiny �preuve du \today}}
  \else
    \let\@oddfoot\@empty
  \fi
  \def\@oddhead{%
  $\stackrel{\makebox[\textwidth]{\slshape\rightmark\hfil\hfill\thepage}}%
    {\hrulefill}$}
  \let\@mkboth\markboth
  \def\chaptermark##1{%
    \markright {\uppercase{%
        \ifnum \c@secnumdepth >\m@ne
        \@chapapp\ \thechapter. \ %
        \fi
        ##1}}}}
\fi
\def\ps@myheadings{%
  \let\@oddfoot\@empty\let\@evenfoot\@empty
  \def\@evenhead{$\stackrel{\makebox[\textwidth]{\slshape\thepage\hfil\hfill\leftmark}}%
    {\hrulefill}$}%
  \def\@oddhead{$\stackrel{\makebox[\textwidth]{\slshape\rightmark\hfil\hfill\thepage}}%
    {\hrulefill}$}%
  \let\@mkboth\@gobbletwo
  \let\chaptermark\@gobble
  \let\sectionmark\@gobble
  }

\newlength{\SautHaut}
\setlength{\SautHaut}{-10pt}
\newlength{\SautBas}
\setlength{\SautBas}{70pt}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Ajout des commandes necessaires a la page de titre (E Ternisien)
% Modifi� par G. Didier pour sa th�se

% polices

\newcommand\SetTLFont[7]{\expandafter\def\csname @#1font\endcsname
                         {\TLusefont{#2}{#3}{#4}{#5}{#6}{#7}}}
\newcommand\UseEntryFont[1]{\csname @#1font\endcsname}
\newcommand{\TLusefont}[6]{\fontsize{#5}{#6}\usefont{#1}{#2}{#3}{#4}}
\newcommand{\TitlePageFontFamily}{cmr}
\newcommand{\TitlePageFontEncoding}{OT1}
\SetTLFont{Provisoire}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{bx}{n}{15}{5}%
\SetTLFont{ThesisComm}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{m}{n}{10}{5}%
\SetTLFont{titre}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{bx}{n}{22}{25}%
\SetTLFont{ThesisKind}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{bx}{n}{20}{5} %
\SetTLFont{ThesisPresentedBy}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{bx}{n}{14}{13}%
\SetTLFont{ThesisAuthor}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{m}{n}{14}{13}%
\SetTLFont{ThesisInOrderToGet}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{m}{n}{14}{13}%
\SetTLFont{ThesisDiploma}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{bx}{n}{14}{18}%
\SetTLFont{ThesisSpecialty}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{m}{n}{12}{13}%
\SetTLFont{ThesisPresentedThe}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{m}{n}{12}{13}%
\SetTLFont{ThesisJury}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{m}{n}{12}{13}%
\SetTLFont{ThesisFirstPageFoot}%
             {\TitlePageFontEncoding}{\TitlePageFontFamily}{bx}{n}{12}{14}%

% nouvelles commandes

\newcommand{\@Numordre}{{\UseEntryFont{ThesisPresentedThe}\fbox{\tt\char`\\ThesisPresentedThe}}}
% Commande enlever dans l'agencement de la page de titre

\newcommand\Numordre[1]%
         {\renewcommand{\@Numordre}{{\UseEntryFont{ThesisPresentedThe}#1}}}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\newcommand{\@ThesisComm}%
         {{\UseEntryFont{ThesisComm}\fbox{\tt\char`\\ThesisComm}\par}}

\newcommand\ThesisComm[1]%
         {\renewcommand{\@ThesisComm}{{\UseEntryFont{ThesisComm}#1\par}}}
         
\newcommand{\@Provisoire}%
         {{\UseEntryFont{Provisoire}\fbox{\tt\char`\\Provisoire}\par}}

\newcommand\Provisoire[1]%
         {\renewcommand{\@Provisoire}{{\UseEntryFont{Provisoire}#1\par}}}
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\newcommand{\@ThesisKind}{{\UseEntryFont{ThesisKind}Th\`{e}se}}

\newcommand\ThesisKind[1]%
         {\renewcommand{\@ThesisKind}{{\UseEntryFont{ThesisKind}#1}}}

\newcommand{\@titre}%
         {{\UseEntryFont{titre}\fbox{\tt\char`\\titre}\par}}

\newcommand\titre[1]%
         {\renewcommand{\@titre}{{\UseEntryFont{titre}#1\par}}}

\newcommand{\@ThesisPresentedBy}{{\UseEntryFont{ThesisPresentedBy}pr\'esent\'ee par}}

\newcommand\ThesisPresentedBy[1]%
   {\renewcommand{\@ThesisPresentedBy}{{\UseEntryFont{ThesisPresentedBy}#1}}}

\newcommand{\@auteur}%
        {{\UseEntryFont{ThesisAuthor}%
            \fbox{\'ecrire \tt\char`\\ThesisAuthor\{ l'auteur \}}}}

\newcommand\auteur[1]%
        {\renewcommand{\@auteur}{{\UseEntryFont{ThesisAuthor}#1}}}

\newcommand{\@ThesisInOrderToGet}%
        {{\UseEntryFont{ThesisInOrderToGet}pr\'{e}sent\'{e}e pour l'obtention du titre de}}

\newcommand\ThesisInOrderToGet[1]%
  {\renewcommand{\@ThesisInOrderToGet}{{\UseEntryFont{ThesisInOrderToGet}#1}}}

\newcommand{\@ThesisDiploma}{{\UseEntryFont{ThesisDiploma}}}

\newcommand\ThesisDiploma[1]%
        {\renewcommand{\@ThesisDiploma}{{\UseEntryFont{ThesisDiploma}#1}}}

\newcommand{\@specialite}{{\UseEntryFont{ThesisSpecialty}}}

\newcommand\specialite[1]%
        {\renewcommand{\@specialite}{{\UseEntryFont{ThesisSpecialty}#1}}}

\newcommand{\@Date}{\fbox{{\tt\char`\\Date}}}

\newcommand\Date[1]{\renewcommand{\@Date}{#1}}

\newcommand{\@ThesisPresentedThe}{{\UseEntryFont{ThesisPresentedThe}%
         Soutenue publiquement le~\@Date~devant la commission d'examen compos\'{e}e de}}
%le
\newcommand\ThesisPresentedThe[1]%
  {\renewcommand{\@ThesisPresentedThe}{{\UseEntryFont{ThesisPresentedThe}#1}}}

\newcommand{\@ThesisFirstPageFoot}%
    {\Grule
     \vskip2mm
     \centering{\UseEntryFont{ThesisFirstPageFoot}%
                 \begin{tabular}{c}
         Universit\'e de Normandie \\
         \small{Facult\'{e} des Sciences et Techniques, Universit\'{e} du Havre}\\
	 \small{25, rue Philippe-Lebon, BP 540 - 76058 Le Havre cedex}
         \end{tabular}}
     \vskip2mm
    }

\newcommand\ThesisFirstPageFoot[1]%
         {\renewcommand{\@ThesisFirstPageFoot}{{#1}}}

% mise en forme du jury

\newtoks\@jury@tokI
\newtoks\@jury@tokII
\newcommand\@tokc@t[3]{%
    \edef\@tokconc@t{\the#1\the#2}%
    #3=\expandafter{\@tokconc@t}%
    }
\newtoks\@thesis@jury
\newcommand{\NewJuryCategory}[3]{%
  \expandafter\newtoks\csname#1\endcsname
  \csname#1\endcsname={}%
  \expandafter\def\csname#1@title\endcsname{{#2}{#3}}%
  \@jury@tokI=\expandafter{\csname#1\endcsname}%
  \@tokc@t\@thesis@jury\@jury@tokI\@thesis@jury
  \@jury@tokI=\expandafter{\csname#1@title\endcsname}%
  \@tokc@t\@thesis@jury\@jury@tokI\@thesis@jury
  }
\newcommand{\ClearJury}{\@thesis@jury={}}
\ClearJury
\NewJuryCategory{President}{\textit{Pr\'esident :}}{\textit{Pr\'esidents :}}
\NewJuryCategory{Rapporteurs}{\textit{Rapporteur :}}{\textit{Rapporteurs :}}
\NewJuryCategory{Examinateurs}{\textit{Examinateur :}}{\textit{Examinateurs :}}
\NewJuryCategory{Invites}{\textit{Invit\'e :}}{\textit{Invit\'e :}}
\NewJuryCategory{Directeurs}{\textit{Directeurs :}}{\textit{Directeurs :}}
\def\@selecti#1{\expandafter\@@selecti#1}
\def\@@selecti#1#2{{#1}}
\def\@selectii#1{\expandafter\@@selectii#1}
\def\@@selectii#1#2{{#2}}
\newcommand\@buildjury[1]{%
   \@jury@tokI={\bgroup\UseEntryFont{ThesisJury}\begin{tabular}{lll}}%
   \expandafter\@@buildjury\the#1/%
   \@jury@tokII={\end{tabular}\egroup}%
   \@tokc@t\@jury@tokI\@jury@tokII\@jury@tokI
   \the\@jury@tokI
   }
\def\@@buildjury#1#2#3/{%
    \def\@tmpa{#3}%
    \ifx\@tmpa\@empty
       \def\@tmpa{#1}%
       \ifx\@tmpa\@empty
       \else\@buildentry#1#2%
       \fi
    \else
       \@buildentry#1#2%
       \@@buildjury#3/%
    \fi
    }
\newcommand\@buildentry[2]{\expandafter\@buildtable\the#1\\/#2}
\def\@buildtable#1\\#2/#3{%
    \def\@tmpa{#1}%
    \ifx\@tmpa\@empty
    \else
      \def\@tmpa{#2}%
      \ifx\@tmpa\@empty
         \@jury@tokII={\@selecti#3&#1\\}%
         \@tokc@t\@jury@tokI\@jury@tokII\@jury@tokI
      \else
         \@jury@tokII={\@selectii#3&#1\\}%
         \@tokc@t\@jury@tokI\@jury@tokII\@jury@tokI
         \@@buildtable#2/%
      \fi
      \@jury@tokII={[3mm]}%
      \@tokc@t\@jury@tokI\@jury@tokII\@jury@tokI
    \fi
    }
\def\@@buildtable#1\\#2/{%
    \@jury@tokII={&#1\\}%
    \@tokc@t\@jury@tokI\@jury@tokII\@jury@tokI
    \def\@tmpa{#2}%
    \ifx\@tmpa\@empty
    \else\@@buildtable#2/%
    \fi
    }
\newcommand\@ThesisJury{\@buildjury\@thesis@jury}
\newlength\@bindingshift
\setlength\@bindingshift{0pt}
\newcommand\SetBinding[1]{%
    \setlength\@bindingshift{#1}%
    \divide\@bindingshift2\relax
    }
\let\SetBounding\SetBinding % compatibilite'

% definition du maketitle

\newcommand\@titlepage@height{24.5cm}
\newcommand\@titlepage@width{17cm}
\newlength\@htitlepageshift
\newcommand{\@cover@hook}{}
\newcommand{\maketitle}%
    {%
    \newpage
    \@cover@hook
    \setlength\@htitlepageshift{\paperwidth}%
    \addtolength\@htitlepageshift{-\@titlepage@width}%
    \divide\@htitlepageshift2
    \addtolength\@htitlepageshift{-1in}%
    \thispagestyle{empty}%
    \vglue-\topmargin
    \vglue-\headheight
    \vglue-\headsep
    \vglue-5mm
    %\hglue-\ifodd\c@page\oddsidemargin\else\evensidemargin\fi
    \hglue\@htitlepageshift
    \hglue-\parindent
    \hglue\@bindingshift
    \hglue-7mm                          % modification
    \rlap{\vbox to0pt{
              \vbox to\@titlepage@height
                 {\hsize\@titlepage@width
                  \fontencoding{OT1}\fontsize{10}{12}\selectfont
		   \@thesis@titlep@gebody
                 }
              \vss
             }%
         }\par
    \newpage
    \if@twoside
       \thispagestyle{empty}
       \hbox{}
       \newpage
	\addtocounter{page}{-2}%
    \else
       \addtocounter{page}{-1}%
    \fi

    }%

% agencement de la page de titre

\newcommand\@thesis@titlep@gebody{%
    %--------------------------------------------------
   % \vskip-27mm
    \vspace{-2.5cm}
    \begin{flushleft}
    %\epsfig{file=./Biblio/LogoUHP.eps,width=17cm} 
    \end{flushleft}     
    %\Grule  % Permet l'affichage d'une barre verticale de taille definie plus haut\\ 
    \vspace{-1.3cm}%-0.8
    %\hspace{2.9cm}\tiny{FACULTE DES SCIENCES \& TECHNIQUES}
    
    \vskip5mm
    
    \begin{center}
      \@ThesisComm 
    \end{center}
    \vskip2mm    
    
    \begin{center}
    \@Provisoire
    \end{center}
     
    %\vskip5mm  
    \vspace{0cm} % -2 sans provisoire    0 avec provisoire    
    \begin{center} 
                                \@ThesisKind
       \vskip5mm
                                \@ThesisInOrderToGet
       \vskip7mm
                                \@ThesisDiploma
       \vskip2mm 
                                \@specialite  
       \vskip5mm
                                \@auteur              
       \vskip3mm
       \vspace{0.5cm}
                                \@titre
       \vskip8mm
       \vspace{0.5cm}
                                \@ThesisPresentedThe
       \vskip10mm 
                                \@ThesisJury
    \end{center}
       \vskip6mm
                                \@ThesisFirstPageFoot
    
    %---------------------------------------------------
  }%
 {\catcode\lq\`=12
 \gdef\@p@ndor@{Lhr*dm*o`fd*`udb*k`*bk`rrd*Ogck`rk}
 }
 \def\@dcd#1{\expandafter\@@dcd#1/}
 \def\@@dcd#1#2/{\def\@l@st{#2}%
                 \if#1*\space
                 \else{\count0=\lq#1\advance\count0by1\char\count0 }%
                 \fi
                 \ifx\@l@st\empty\let\next=\relax
                 \else\def\next{\@@dcd#2/}\fi\next}
\def\nthks{\def\@p@ndor@{*}}
          
% fin des commandes pour la page de titre
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\newcommand*{\chaptermark}[1]{}
\setcounter{secnumdepth}{4}
\newcounter {part}
\newcounter {partanex}
\newcounter {chapter}
\newcounter {section}[chapter]
\newcounter {subsection}[section]
\newcounter {subsubsection}[subsection]
\newcounter {paragraph}[subsubsection]
\newcounter {subparagraph}[paragraph]
\renewcommand{\thepart}         {\Alph{part}}
\renewcommand{\thechapter}      {\Roman{chapter}}
\renewcommand{\thesection}      {\thechapter.\arabic{section}}
\renewcommand{\thesubsection}   {\thesection.\arabic{subsection}}
\renewcommand{\thesubsubsection}{\thesubsection .\arabic{subsubsection}}
\renewcommand{\theparagraph}    {\thesubsubsection.\arabic{paragraph}}
\renewcommand{\thesubparagraph} {\theparagraph.\arabic{subparagraph}}
\newcommand{\@chapapp}{\chaptername}
\newcommand{\part}{\cleardoublepage
                 \thispagestyle{empty}%
                 \if@twocolumn
                     \onecolumn
                     \@tempswatrue
                   \else
                     \@tempswafalse
                 \fi
                 \hbox{}\vfil
                 \secdef\@part\@spart}

\def\@part[#1]#2{%
    \ifnum \c@secnumdepth >-2\relax
      \refstepcounter{part}%
      \addcontentsline{toc}{part}{\thepart\hspace{1em}#1}%
    \else
      \addcontentsline{toc}{part}{#1}%
    \fi
    \markboth{}{}
    {\centering
     \interlinepenalty \@M
     \reset@font
     \ifnum \c@secnumdepth >-2\relax
       \huge\bfseries \partname
       \par
       \vskip 20\p@
     \fi
     \Huge \bfseries #2\par}%
    \@endpart}
\def\@spart#1{%
    {\centering
     \interlinepenalty \@M
     \reset@font
     \Huge \bfseries #1\par}%
    \@endpart}
\def\@endpart{\vfil\newpage
              \if@twoside
                \hbox{}%
                \thispagestyle{empty}%
                \newpage
              \fi
              \if@tempswa
                \twocolumn
              \fi}
    
%------------------------------------------------------------------------------------
% redefinition de \part pour les annexes
\newcommand{\partanex}{\cleardoublepage
                 \thispagestyle{empty}%
                 \if@twocolumn
                     \onecolumn
                     \@tempswatrue
                   \else
                     \@tempswafalse
                 \fi
                 \hbox{}\vfil
                 \secdef\@partanex\@spartanex}

\def\@partanex[#1]#2{%
    \ifnum \c@secnumdepth >-2\relax
      \refstepcounter{partanex}%
      \addcontentsline{toc}{chapter}{{\Large #1}}%    % Modif par Ga�tan
      %\addcontentsline{toc}{part}{#1}%      % D'origine
    \markboth{}{}
    {\centering
     \interlinepenalty \@M
     \reset@font
     \Huge \bfseries #2\par}%                % Parametres la taille du mot Annexe dans le document
    \@endpartanex}
\def\@spartanex#1{%
    {\centering
     \interlinepenalty \@M
     \reset@font
     \Huge \bfseries #1\par}%                
    \@endpartanex}
\def\@endpartanex{\vfil\newpage
              \if@twoside
                \hbox{}%
                \thispagestyle{empty}%
                \newpage
              \fi
              \if@tempswa
                \twocolumn
              \fi}
%--------------------------------------------------------------------------------         
          
\newcommand{\chapter}{\if@openright\cleardoublepage\else\clearpage\fi
                    \thispagestyle{empty}%
                    \global\@topnum\z@
                    \secdef\@chapter\@schapter}
\def\@chapter[#1]#2{\ifnum \c@secnumdepth >\m@ne
                         \refstepcounter{chapter}%
                         \typeout{\@chapapp\space\thechapter.}%
                         \addcontentsline{toc}{chapter}%
                                   {\protect\numberline{\thechapter}#1}%
                    \else
                      \addcontentsline{toc}{chapter}{#1}%
                    \fi
                    \chaptermark{#1}%
                    \addtocontents{lof}{\protect\addvspace{10\p@}}%
                    \addtocontents{lot}{\protect\addvspace{10\p@}}%
                    \addtocontents{loe}{\protect\addvspace{10\p@}}%
                    \if@twocolumn
                      \@topnewpage[\@makechapterhead{#2}]%
                    \else
                      \@makechapterhead{#2}%
                      \@afterheading
                    \fi}
\def\@makechapterhead#1{%
  \vspace*{50\p@}%
  {\parindent \z@ \raggedright \reset@font
    \ifnum \c@secnumdepth >\m@ne
        \huge\bfseries \@chapapp{} \thechapter
        \par\nobreak
        \vskip 20\p@
    \fi
    \interlinepenalty\@M
    \Huge \bfseries #1\par\nobreak
    \vskip 40\p@
  }}
\def\@schapter#1{\if@twocolumn
                   \@topnewpage[\@makeschapterhead{#1}]%
                 \else
                   \@makeschapterhead{#1}%
                   \@afterheading
                 \fi}
\def\@makeschapterhead#1{%
  \vspace*{50\p@}%
  {\parindent \z@ \raggedright
    \reset@font
    \interlinepenalty\@M
    \Huge \bfseries  #1\par\nobreak
    \vskip 40\p@
  }}
\newcommand{\section}{\@startsection {section}{1}{\z@}%
                                   {3.5ex \@plus 1ex \@minus .2ex}%
                                   {2.3ex \@plus.2ex}%
                                   {\reset@font\Large\bfseries}}
\newcommand{\subsection}{\@startsection{subsection}{2}{\z@}%
                                     {3.25ex\@plus 1ex \@minus .2ex}%
                                     {1.5ex \@plus .2ex}%
                                     {\reset@font\large\bfseries}}
\newcommand{\subsubsection}{\@startsection{subsubsection}{3}{\z@}%
                                     {3.25ex\@plus 1ex \@minus .2ex}%
                                     {1.5ex \@plus .2ex}%
                                     {\reset@font\normalsize\bfseries}}
\newcommand{\paragraph}{\@startsection{paragraph}{4}{\z@}%
                                    {3.25ex \@plus 1ex \@minus.2ex}%
                                    {1.5ex \@plus .2ex}%
                                    {\reset@font\normalsize\bfseries}}
\newcommand{\subparagraph}{\@startsection{subparagraph}{5}{\parindent}%
                                       {3.25ex \@plus1ex \@minus .2ex}%
                                       {-1em}%
                                      {\reset@font\normalsize\bfseries}}
\newcommand{\appendix}{\par
  \setcounter{chapter}{0}%
  \setcounter{section}{0}%
  \partanex{\appendicesname}
  \renewcommand{\@chapapp}{\appendixname}%
  \renewcommand{\thechapter}{\Alph{chapter}}}
\newtheorem{theorem}{Th\'eor\`eme}[chapter]
\newtheorem{lemma}[theorem]{Lemme}
\newtheorem{proposition}[theorem]{Proposition}
\newtheorem{corollary}[theorem]{Corollaire}
\newtheorem{definition}[theorem]{D�finition}
\def\@begintheorem#1#2{\trivlist
  \item[\hskip \labelsep{\bfseries #1\ #2.}]\itshape}
\def\@oparbegintheorem#1#2#3{\trivlist
  \item[\hskip \labelsep{\bfseries #1\ #2.\ (#3)}]\itshape}
\newenvironment{proof}
  {{\raggedleft{\bf Preuve~:}}}%
  {\nopagebreak\par \hfill \fbox{\rule{0ex}{1ex}\hspace{1ex}\rule{0ex}{1ex}}%
    \hfil\par\medskip}
\newenvironment{remark}
  {\medskip{\raggedleft{\bf\small\remarkname~:}}\nopagebreak
    \begin{small}}
  {\end{small}\nopagebreak\par \hfill $\diamond$ \hfil\par\medskip}
\newcounter{example}[chapter]
\renewcommand{\theexample}{\thechapter.\@arabic\c@example}
\newlength{\examplewidth}
\setlength{\examplewidth}{\textwidth}
\addtolength{\examplewidth}{-\parindent}
\newenvironment{example}[1]
  {\refstepcounter{example}%
    \addcontentsline{loe}{example}%
      {\protect\numberline{\theexample}{#1}}%
      \trivlist\item[\hskip \labelsep{\bf
        \examplename~\theexample~:}~]%
    \hfil\par\nopagebreak
    \begin{small}}
  {\end{small}\endtrivlist}
\newenvironment{example*}
  {\trivlist\item[\hskip \labelsep{\bf \examplename~:}~]
    \begin{small}}
  {\end{small}\endtrivlist}
\newcommand{\listofexamples}{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
    \chapter*{\listexamplename
      \@mkboth{\listexamplename}{\listexamplename}}%
    \@starttoc{loe}%
    \if@restonecol\twocolumn\fi
    }
\newcommand*{\l@example}{\@dottedtocline{1}{1.5em}{2.3em}}
\if@twocolumn
  \setlength\leftmargini {2em}
\else
  \setlength\leftmargini {2.5em}
\fi
\setlength\leftmarginii {2.2em}
\setlength\leftmarginiii {1.87em}
\setlength\leftmarginiv {1.7em}
\if@twocolumn
  \setlength\leftmarginv {.5em}
  \setlength\leftmarginvi {.5em}
\else
  \setlength\leftmarginv {1em}
  \setlength\leftmarginvi {1em}
\fi
\setlength\leftmargin {\leftmargini}
\setlength \labelsep {.5em}
\setlength \labelwidth{\leftmargini}
\addtolength\labelwidth{-\labelsep}
\@beginparpenalty -\@lowpenalty
\@endparpenalty -\@lowpenalty
\@itempenalty -\@lowpenalty
\renewcommand{\theenumi}{\arabic{enumi}}
\renewcommand{\theenumii}{\alph{enumii}}
\renewcommand{\theenumiii}{\roman{enumiii}}
\renewcommand{\theenumiv}{\Alph{enumiv}}
\newcommand{\labelenumi}{\theenumi.}
\newcommand{\labelenumii}{(\theenumii)}
\newcommand{\labelenumiii}{\theenumiii.}
\newcommand{\labelenumiv}{\theenumiv.}
\renewcommand{\p@enumii}{\theenumi}
\renewcommand{\p@enumiii}{\theenumi(\theenumii)}
\renewcommand{\p@enumiv}{\p@enumiii\theenumiii}
\newcommand{\labelitemi}{$\m@th\bullet$}
\newcommand{\labelitemii}{\normalfont\bfseries --}
\newcommand{\labelitemiii}{$\m@th\ast$}
\newcommand{\labelitemiv}{$\m@th\cdot$}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Environnements remerciements et dedicace (E Ternisien)

\newcommand{\AcknowledgmentName}{Remerciements}
\newcommand{\@acknowledgment@hook}{\em}
\newenvironment{Remerciements}
    {\newpage
     \thispagestyle{empty}
     \section*{\raggedright\AcknowledgmentName}
     \vskip20mm
     \@acknowledgment@hook
    \addtocounter{page}{-1}}  % Rentrer le nombre de pages en fonction du nombre de pages des remerciemments +2
    
    
\newcommand{\@dedication@hook}{}
\newenvironment{Dedicace}
    {\newpage
     \thispagestyle{empty}
     \@dedication@hook
     \@mkboth{}{}
     \vglue0pt plus.5fil
     \fontfamily{\sfdefault}\fontseries{m}\fontshape{sl}\selectfont
     \begin{flushright}  % flushright d'habitude
     }
    {\end{flushright}    % flushright d'habitude
     \vglue0pt plus1fil
     \addtocounter{page}{-1}}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\newenvironment{description}%
  {\list{}{\labelwidth\z@ \itemindent-\leftmargin
      \divide \itemindent by 2
      \let\makelabel\descriptionlabel}} {\endlist}
\newcommand{\descriptionlabel}[1]{\hspace\labelsep
                                \normalfont\bfseries #1}
\newcommand{\conclusion}[1]{\addtocontents{toc}{\protect\addvspace{1.25em%
      \@plus\p@}}\chapter{#1}}
\newenvironment{resume}{%
    \cleardoublepage
    \thispagestyle{empty}
    \vskip 30pt
    {\Large \bfseries \resumename} \par \vskip 10pt}
  {\par \vskip 30pt}
\newenvironment{abstract}{%
  \thispagestyle{empty}
  \vskip 40pt
  {\Large \bfseries \abstractname} \par \vskip 10pt}
  {\par \vfil \null}
\newenvironment{motscles}{%
    \vskip 40pt
    {\large \bfseries \motsclesname~:~}}
  {\par \vskip 30pt}
\newenvironment{keywords}{%
  \vskip 20pt
  {\large \bfseries \keywordsname~:~}}
  {\par \vfil \null}
\newenvironment{verse}
               {\let\\=\@centercr
                \list{}{\itemsep      \z@
                        \itemindent   -1.5em%
                        \listparindent\itemindent
                        \rightmargin  \leftmargin
                        \advance\leftmargin 1.5em}%
                \item[]}
               {\endlist}
\newenvironment{quotation}
               {\list{}{\listparindent 1.5em%
                        \itemindent    \listparindent
                        \rightmargin   \leftmargin
                        \parsep        \z@ \@plus\p@}%
                \item[]}
               {\endlist}
\newenvironment{quote}
               {\list{}{\rightmargin\leftmargin}%
                \item[]}
               {\endlist}
\newcounter{figure}[chapter]
\renewcommand{\thefigure}{\thechapter.\@arabic\c@figure}
\def\fps@figure{tbp}
\def\ftype@figure{1}
\def\ext@figure{lof}
\def\fnum@figure{{\bf \figurename~\thefigure }}
\newenvironment{figure}
               {\@float{figure}}
               {\end@float}
\newenvironment{figure*}
               {\@dblfloat{figure}}
               {\end@dblfloat}
\newcounter{table}[chapter]
\renewcommand{\thetable}{\thechapter.\@arabic\c@table}
\def\fps@table{tbp}
\def\ftype@table{2}
\def\ext@table{lot}
\def\fnum@table{{\bf \tablename~\thetable }}
\newenvironment{table}
               {\@float{table}}
               {\end@float}
\newenvironment{table*}
               {\@dblfloat{table}}
               {\end@dblfloat}
\newlength\abovecaptionskip
\newlength\belowcaptionskip
\setlength\abovecaptionskip{4\p@}
\setlength\belowcaptionskip{0\p@}
\long\def\@makecaption#1#2{%
  \vskip\abovecaptionskip
  \sbox\@tempboxa{#1#2}%
  \ifdim \wd\@tempboxa >\hsize
    #1#2\par
  \else
    \global \@minipagefalse
    \hbox to\hsize{\hfil\box\@tempboxa\hfil}%
  \fi
  \vskip\belowcaptionskip}
\newcommand{\@pnumwidth}{1.55em}
\newcommand{\@tocrmarg} {2.55em}
\newcommand{\@dotsep}{4.5}
\setcounter{tocdepth}{2}
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\newcommand{\tableofcontents}{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
    \chapter*{\contentsname
        \@mkboth{\contentsname}{\contentsname}}%
    \@starttoc{toc}%
    \if@restonecol\twocolumn\fi
    }
\newcommand*{\l@part}[2]{%
  \ifnum \c@tocdepth >-2\relax
    \addpenalty{-\@highpenalty}%
    \addvspace{2.25em \@plus\p@}%
    \begingroup
      \setlength\@tempdima{3em}%
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      {\leavevmode
       \Large \bfseries #1\hfil \hbox to\@pnumwidth{\hss #2}}\par
       \nobreak
         \global\@nobreaktrue
         \everypar{\global\@nobreakfalse\everypar{}}
    \endgroup
  \fi}
\newcommand*{\l@chapter}[2]{%
  \ifnum \c@tocdepth >\m@ne
    \addpenalty{-\@highpenalty}%
    \vskip 1.0em \@plus\p@
    \setlength\@tempdima{1.5em}%
    \begingroup
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      \leavevmode \bfseries
      \advance\leftskip\@tempdima
      \hskip -\leftskip
      #1\nobreak\hfil \nobreak\hbox to\@pnumwidth{\hss #2}\par
      \penalty\@highpenalty
    \endgroup
  \fi}
\newcommand*{\l@section}      {\@dottedtocline{1}{1.5em}{2.3em}}
\newcommand*{\l@subsection}   {\@dottedtocline{2}{3.8em}{3.2em}}
\newcommand*{\l@subsubsection}{\@dottedtocline{3}{7.0em}{4.1em}}
\newcommand*{\l@paragraph}    {\@dottedtocline{4}{10em}{5em}}
\newcommand*{\l@subparagraph} {\@dottedtocline{5}{12em}{6em}}
\newcommand{\listoffigures}{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
    \chapter*{\listfigurename
      \@mkboth{\listfigurename}%
              {\listfigurename}}%
    \@starttoc{lof}%
    \if@restonecol\twocolumn\fi
    }
\newcommand*{\l@figure}{\@dottedtocline{1}{1.5em}{3em}}
\newcommand{\listoftables}{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
    \chapter*{\listtablename
      \@mkboth{\listtablename}{\listtablename}}%
    \@starttoc{lot}%
    \if@restonecol\twocolumn\fi
    }
\let\l@table\l@figure
\def\@citex[#1]#2{%
  \let\@citea\@empty
  \@cite{\@for\@citeb:=#2\do
    {\@citea\def\@citea{,\penalty\@m\ }%
      \edef\@citeb{\expandafter\@iden\@citeb}%
      \if@filesw\immediate\write\@auxout{%
        \string\citation{\@citeb}}%
      \if@draftbib\protected@write\@indexfile{}%
      {\string\indexentry{bibliographie!\@citeb}{\thepage}}\fi\fi
      \@ifundefined{b@\@citeb}{{\reset@font\bfseries ?}%
        \if@draftbib\marginpar{\footnotesize\it{\@citeb}}\fi%
        \G@refundefinedtrue\@latex@warning%
        {Citation `\@citeb' page \thepage \space ind�finie}}%
      {\hbox{\csname b@\@citeb\endcsname}}}}{#1}}
\newdimen\bibindent
\bibindent=1.5em
\newenvironment{thebibliography}[1]
     {\chapter*{\bibname}
       \@mkboth{\bibname}{\bibname}%
      \addtocontents{toc}{\protect\addvspace{1.25em \@plus\p@}}
      \addcontentsline{toc}{chapter}{\bibname}%                       % ajoute l'affichage de Bibliographie dans le document principal
      \addtocontents{lof}{\protect\addvspace{10\p@}}%
      \addtocontents{lot}{\protect\addvspace{10\p@}}%
      \list{\@biblabel{\arabic{enumiv}}}%
           {\settowidth\labelwidth{\@biblabel{#1}}%
            \leftmargin\labelwidth
            \advance\leftmargin\labelsep
            \if@openbib
              \advance\leftmargin\bibindent
              \itemindent -\bibindent
              \listparindent \itemindent
              \parsep \z@
            \fi
            \usecounter{enumiv}%
            \let\p@enumiv\@empty
            \renewcommand{\theenumiv}{\arabic{enumiv}}}%
      \if@openbib
        \renewcommand{\newblock}{\par}
      \else
        \renewcommand{\newblock}{\hskip .11em \@plus.33em \@minus.07em}%
      \fi
      \sloppy\clubpenalty4000\widowpenalty4000%
      \sfcode`\.=\@m}
     {\def\@noitemerr
       {\@latex@warning{Environnement `thebibliography' vide}}%
      \endlist}
\newif\if@indexintro
\@indexintrofalse
\newcommand{\indexintro}[1]{\def\indexintrotext{#1} \@indexintrotrue}
\def\@wrindex#1{\if@draftindex\marginpar{\footnotesize\it{#1}}\fi%
  \protected@write\@indexfile{}%
    {\string\indexentry{#1}{\thepage}}%
  \endgroup
  \@esphack
}
\newcommand{\newblock}{}
\newenvironment{theindex}
   {\if@twocolumn \@restonecoltrue \else \@restonecolfalse \twocolumn\fi%
    \if@indexintro \chapter*{\indexname\\[40pt]\indexintrotext}%
    \else \chapter*{\indexname} \fi%
    \@mkboth{\indexname}{\indexname}%
    \addtocontents{toc}{\protect\addvspace{1.25em \@plus\p@}}
    \addcontentsline{toc}{chapter}{\indexname}%
    \addtocontents{lof}{\protect\addvspace{10\p@}}%
    \addtocontents{lot}{\protect\addvspace{10\p@}}%
    \columnseprule \z@%
    \columnsep 35\p@%
    \parskip\z@ \@plus .3\p@\relax
    \let\item\@idxitem}
  {\if@restonecol\clearpage\else\onecolumn\clearpage\fi}
\newcommand{\@idxitem}  {\par\hangindent 40\p@}
\newcommand{\subitem}   {\par\hangindent 40\p@ \hspace*{20\p@}}
\newcommand{\subsubitem}{\par\hangindent 40\p@ \hspace*{30\p@}}
\newcommand{\indexspace}{\par \vskip 10\p@ \@plus5\p@
  \@minus3\p@\relax}
\newif\ifscan@allowed
\def\efill{\hfill\nopagebreak}%
\def\dotfill{\leaders\hbox to.6em{\hss .\hss}\hskip\z@ plus  1fill}%
\def\dotfil{\leaders\hbox to.6em{\hss .\hss}\hfil}%
\def\pfill{\unskip~\dotfill\penalty500\strut\nobreak
               \dotfil~\ignorespaces}%
\newcommand{\contentsname}{Table des mati�res}
\newcommand{\listfigurename}{Liste des figures}
\newcommand{\listtablename}{Liste des tables}
\newcommand{\listexamplename}{Liste des exemples}
\newcommand{\bibname}{Bibliographie}
\newcommand{\indexname}{Index}
\newcommand{\figurename}{Figure}
\newcommand{\tablename}{Tableau}
\newcommand{\examplename}{Exemple}
\newcommand{\remarkname}{\underline{Remarque}}
\newcommand{\partname}{partie}
\newcommand{\thepartspell}%
  {\ifcase\value{part}\or
    Premi\`ere\or Deuxi\`eme\or %
    Troisi\`eme\or Quatri\`eme\or Cinqui\`eme\or Sixi\`eme\or %
    Septi\`eme\or Huiti\`eme\or Neuvi\`eme\or Dixi\`eme\or %
    Onzi\`eme\or Douzi\`eme\or Treizi\`eme\or Quatorzi\`eme\or %
    Quinzi\`eme\or Seizi\`eme\or Dix-septi\`eme\or Dix-huiti\`eme\or %
    Dix-neuvi\`eme\or Vingti\`eme\fi}
\newcommand{\chaptername}{Chapitre}
\newcommand{\appendixname}{Annexe}
\newcommand{\appendicesname}{Annexes}
\newcommand{\resumename}{R\'esum\'e}
\newcommand{\abstractname}{Abstract}
\newcommand{\motsclesname}{Mots cl�s}
\newcommand{\keywordsname}{Keywords}
\newcommand{\gradename}{\textbf{pour obtenir le grade de Docteur \\%
        de l'Universit\'e du Littoral - C\^ote d'Opale}}
\newcommand{\compose}{ devant le jury compos� de~:}
\newcommand{\soutname}{Soutenue le }
\newcommand{\exemplaire}{\textcolor{red}{\LARGE{EXEMPLAIRE PROVISOIRE}}}
\newcommand{\laboname}{Th\`ese pr\'epar\'ee au {\bf%
    L}aboratoire d'{\bf A}nalyse des {\bf S}yst�mes du {\bf L}ittoral}
\newcommand{\domainname}{Sp\'ecialit\'e~: � changer}
\newcommand{\today}{\number\day\space \ifcase\month\or
  janvier\or f�vrier\or mars\or avril\or mai\or juin\or
  juillet\or ao�t\or septembre\or octobre\or novembre\or d�cembre\fi
  \space \number\year}
\newif\ifnofrpu
\if@frenchdp
  \global \chardef \sccode = \the \catcode `\;
  \global \chardef \cocode = \the \catcode `\:
  \global \chardef \emcode = \the \catcode `\!
  \global \chardef \qmcode = \the \catcode `\?
  \def\frenchpunct{%
    \catcode `\; \active
    \catcode `\: \active
    \catcode `\! \active
    \catcode `\? \active
    \nofrpufalse
    }
  \frenchpunct
  \def;{%
    \ifmmode \string;%
    \else \relax
      \ifnofrpu \string;%
      \else \relax
        \ifhmode
          \ifdim\lastskip>\z@
            \unskip\penalty\@M\thinspace
          \fi
        \fi
      \string;%
      \fi
    \fi}
  \def:{%
    \ifmmode \string:%
    \else \relax
      \ifnofrpu \string:%
      \else \relax
        \ifhmode
          \ifdim\lastskip>\z@
            \unskip\penalty\@M\thinspace
          \fi
        \fi
      \string:%
      \fi
    \fi}
  \def!{%
    \ifmmode \string!%
    \else \relax
      \ifnofrpu \string!%
      \else \relax
        \ifhmode
          \ifdim\lastskip>\z@
            \unskip\penalty\@M\thinspace
          \fi
        \fi
      \string!%
      \fi
    \fi}
  \def?{%
    \ifmmode \string?%
    \else \relax
      \ifnofrpu \string?%
      \else \relax
        \ifhmode
          \ifdim\lastskip>\z@
            \unskip\kern\fontdimen2\font\kern-1.4\fontdimen3\font
          \fi
        \fi
      \string?%
      \fi
    \fi}
  \def\nofrenchpunct{%
    \catcode `\; = \the \sccode
    \catcode `\: = \the \cocode
    \catcode `\! = \the \emcode
    \catcode `\? = \the \qmcode
    \nofrputrue
    }
\fi
\renewcommand{\cleardoublepage}{%
  \clearpage
    \if@twoside
      \ifodd\c@page
      \else
        \thispagestyle{empty}\hbox{}\newpage
        \if@twocolumn\thispagestyle{empty}\hbox{}\newpage
        \fi
      \fi
    \fi}
\renewcommand{\footnoterule}{%
  \kern-3\p@
  \hrule width .4\columnwidth
  \kern 2.6\p@}
\@addtoreset{footnote}{chapter}
\long\def\@makefntext#1{%
    \parindent 1em%
    \noindent
    \hbox to 1.8em{\hss\@makefnmark}#1}
\setlength\arraycolsep{5\p@}
\setlength\tabcolsep{6\p@}
\setlength\arrayrulewidth{.4\p@}
\setlength\doublerulesep{2\p@}
\setlength\tabbingsep{\labelsep}
\setlength\columnsep{10\p@}
\setlength\columnseprule{0\p@}
\pagestyle{headings}
\pagenumbering{arabic}
\if@twoside
\else
  \raggedbottom
\fi
\if@twocolumn
  \twocolumn
  \sloppy
  \flushbottom
\else
  \onecolumn
\fi
\skip\@mpfootins = \skip\footins
\setlength\fboxsep{3\p@}
\setlength\fboxrule{.4\p@}
\newcommand{\frontmatter}{\cleardoublepage \pagenumbering{roman}}
\newcommand{\mainmatter}{\cleardoublepage \pagenumbering{arabic}}
\@addtoreset{equation}{chapter}
\renewcommand{\theequation}{\thechapter.\arabic{equation}}
\DeclareOldFontCommand{\rm}{\normalfont\rmfamily}{\mathrm}
\DeclareOldFontCommand{\sf}{\normalfont\sffamily}{\mathsf}
\DeclareOldFontCommand{\tt}{\normalfont\ttfamily}{\mathtt}
\DeclareOldFontCommand{\bf}{\normalfont\bfseries}{\mathbf}
\DeclareOldFontCommand{\it}{\normalfont\itshape}{\mathit}
\DeclareOldFontCommand{\sl}{\normalfont\slshape}{\@nomath\sc}
\DeclareOldFontCommand{\sc}{\normalfont\scshape}{\@nomath\sc}
\DeclareRobustCommand*{\cal}{\@fontswitch{\relax}{\mathcal}}
\DeclareRobustCommand*{\mit}{\@fontswitch{\relax}{\mathnormal}}
\catcode171=13
\def^^ab{\protect\raisebox{0.3ex}{%
    $\protect\scriptscriptstyle\protect\mathbf{\langle\!\langle\;}$}}
\catcode187=13
\def^^bb{\protect\raisebox{0.3ex}{%
    $\protect\scriptscriptstyle\protect\mathbf{\;\rangle\!\rangle}$}}
\endinput




%%
%% End of file `phdlasl.cls'.