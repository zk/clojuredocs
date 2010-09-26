require "mkmf"

dir_config("levenshtein")

have_library("levenshtein_array")
have_library("levenshtein_array_of_strings")
have_library("levenshtein_generic")
have_library("levenshtein_string")

create_makefile("levenshtein/levenshtein_fast")
