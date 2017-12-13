PROJECT=mgr
PROJECT_VIEW=${PROJECT}_view
OUT=build
LATEX=pdflatex -interaction nonstopmode -halt-on-error -file-line-error

all: clean build

build: latex1 bibligrafia latex2 cpview

latex1:
	${LATEX} --output-directory=${OUT} ${PROJECT}

latex2:
	${LATEX} --output-directory=${OUT} ${PROJECT}
	
bibligrafia:
	biber ${OUT}/${PROJECT}

show:
	okular ${OUT}/${PROJECT}.pdf

cpview:
	cp ${OUT}/${PROJECT}.pdf ${OUT}/${PROJECT_VIEW}.pdf

watch:
	watchtower -i 2 -f '*.tex' -x '*/*' -e make

clean:
	rm -f ${OUT}/*
