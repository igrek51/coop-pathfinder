PROJECT=praca
PROJECT_VIEW=${PROJECT}_view
OUT=build
LATEX=pdflatex -interaction nonstopmode -halt-on-error -file-line-error

all: clean build

build: latex1 bibgen latex2 latex3 success

latex:
	${LATEX} --output-directory=${OUT} ${PROJECT}
	cp "${OUT}/${PROJECT}.pdf" "${OUT}/${PROJECT_VIEW}.pdf"

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
	okular ${OUT}/${PROJECT_VIEW}.pdf &

watch:
	watchtower -i 2 -f '**/*.tex' 'bibliography.bib' -e make

success:
	# replace file when it's actually ready to prevent spurious reloadings by okular
	cp "${OUT}/${PROJECT}.pdf" "${OUT}/${PROJECT_VIEW}.pdf"
	@echo "${OUT}/${PROJECT_VIEW}.pdf has been generated successfully."

spell:
	# aspell -t --lang=pl -c praca.tex
	# cat *.tex */*.tex | aspell list -t --lang=pl | sort | uniq
	cat */*.tex | aspell list -t --lang=pl | sort | uniq

clean:
	rm -f ${OUT}/*
