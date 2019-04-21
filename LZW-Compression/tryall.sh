#! /bin/bash

if [ "$1" = "-" ]; then
	for f in ./ex/*; do
		fname=$(basename -- "$f")
		echo "Compressing $fname with $2 mode"
		java MyLZW $1 $2 < $f > ./test.out/$fname.$2.lzw
	done

elif [ "$1" = "+" ]; then
	for f in ./test.out/*.$2.lzw; do
		fname=$(basename -- "$f")
		fname="${fname%.$2.lzw}"
		echo "Expanding $fname with $2 mode"
		java MyLZW $1 < $f > ./test.in/$2.$fname
	done

elif [ "$1" = "diff" ]; then
	for f in ./ex/*; do
		fname=$(basename -- "$f")
		diff -q $f ./test.in/$2.$fname
	done

elif [ "$1" = "size" ]; then
	mode=("n"  "m" "r")
	for m in "${mode[@]}"; do
		for f in ./test.out/*.$m.lzw; do
			fname=$(basename -- "$f")
			fname=${fname%.$m.lzw}
		       	echo "Compressed $fname with $m mode: "
			wc -c $f | grep -o "^[0-9]*" | tr -d "\n"
			echo " bytes"
		done
	done

elif [ "$1" = "clear" ]; then
	for f in ./test.out/*; do
		rm -rf $f
	done
	for f in ./test.in/*; do
		rm -rf $f
	done

elif [ "$1" = "all" ]; then
	./tryall.sh clear
	mode=("n" "m" "r")
	for m in "${mode[@]}"; do
		./tryall.sh - $m
		./tryall.sh + $m
		./tryall.sh diff $m
	done

fi
