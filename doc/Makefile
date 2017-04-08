PROJECT=mgr
PROJECT_VIEW=${PROJECT}_view
#OUT=.
OUT=build
LATEX=pdflatex -interaction nonstopmode -halt-on-error -file-line-error

all: clean build

build: latex1 bibligrafia latex2 view

latex1:
	${LATEX} --output-directory=${OUT} ${PROJECT}

latex2:
	${LATEX} --output-directory=${OUT} ${PROJECT}

latex3:
	${LATEX} --output-directory=${OUT} ${PROJECT}
	
bibligrafia:
	biber ${OUT}/${PROJECT}

show:
	okular ${OUT}/${PROJECT}.pdf

view:
	cp ${OUT}/${PROJECT}.pdf ${OUT}/${PROJECT_VIEW}.pdf

clean:
	rm -f ${OUT}/*
