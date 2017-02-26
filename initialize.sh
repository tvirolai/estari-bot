#!/bin/bash

if [ ! -d data ]; then
  mkdir data
fi

echo "Downloading TurkuNLP's Finnish sentence detector (fi-sent.bin)"

wget https://github.com/TurkuNLP/Finnish-dep-parser/raw/master/model/fi-sent.bin -o ./data/fi-sent.bin &&

echo "Downloading TurkuNLP's Finnish tokenizer (fi-token.bin)"

wget https://github.com/TurkuNLP/Finnish-dep-parser/raw/master/model/fi-token.bin -o ./data/fi-token.bin &&

echo "Downloading a list of Finnish stopwords (fi_stopwords.txt)"

wget https://github.com/satybald/calc_es_lda/raw/master/stopwords/fi_stopwords.txt -o ./data/fi_stopwords.txt

echo "Done."
