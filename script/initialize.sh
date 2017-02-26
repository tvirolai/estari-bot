#!/bin/bash

script_dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
data_dir="${script_dir}/../data"

url_sent="https://raw.githubusercontent.com/TurkuNLP/Finnish-dep-parser/master/model/fi-sent.bin"
url_token="https://raw.githubusercontent.com/TurkuNLP/Finnish-dep-parser/master/model/fi-token.bin"
url_stopwords="https://raw.githubusercontent.com/satybald/calc_es_lda/master/stopwords/fi_stopwords.txt"

mkdir -p "${data_dir}"

echo "Downloading TurkuNLP's Finnish sentence detector (fi-sent.bin)"

wget $url_sent -P "${data_dir}"

echo "Downloading TurkuNLP's Finnish tokenizer (fi-token.bin)"

wget $url_token -P "${data_dir}"

echo "Downloading a list of Finnish stopwords (fi_stopwords.txt)"

wget $url_stopwords -P "${data_dir}"

echo "Done."
