PROJECT=trans
PROJECT_VIEW=${PROJECT}_view
OUT=build
LATEX=pdflatex -interaction nonstopmode -halt-on-error -file-line-error

all: clean build

build: latex1 bibgen latex2 latex3

latex1:
	@echo "========================== pdflatex (1) =========================="
	${LATEX} --output-directory=${OUT} ${PROJECT}

latex2:
	@echo "========================== pdflatex (2) =========================="
	${LATEX} --output-directory=${OUT} ${PROJECT} 1>/dev/null

latex3:
	@echo "========================== pdflatex (3) =========================="
	${LATEX} --output-directory=${OUT} ${PROJECT} 1>/dev/null
	
bibgen:
	@echo "============================ bibtex =============================="
	bibtex ${OUT}/${PROJECT}

show:
	okular ${OUT}/${PROJECT}.pdf &

watch:
	watchtower -i 2 -f '**/*.tex' bibliography.bib -e make

clean:
	rm -f ${OUT}/*
