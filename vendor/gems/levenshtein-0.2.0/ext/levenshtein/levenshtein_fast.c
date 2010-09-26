#include "ruby.h"

VALUE levenshtein_distance_fast(VALUE self, VALUE rb_o1, VALUE rb_o2, VALUE rb_threshold) {
  if ((TYPE(rb_o1) == T_STRING) && (TYPE(rb_o2)) == T_STRING) {
    return levenshtein_distance_string(self, rb_o1, rb_o2, rb_threshold);
  } else if ((TYPE(rb_o1) == T_ARRAY) && (TYPE(rb_o2)) == T_ARRAY) {
    if ((TYPE(rb_ary_entry(rb_o1, 0)) == T_STRING) && (TYPE(rb_ary_entry(rb_o2, 0))) == T_STRING) {
      return levenshtein_distance_array_of_strings(self, rb_o1, rb_o2, rb_threshold);
    } else {
      return levenshtein_distance_array(self, rb_o1, rb_o2, rb_threshold);
    }
  } else {
    return levenshtein_distance_generic(self, rb_o1, rb_o2, rb_threshold);
  }
}

void Init_levenshtein_fast() {
  VALUE mLevenshtein	= rb_define_module("Levenshtein");

  rb_define_singleton_method(mLevenshtein, "levenshtein_distance_fast" , levenshtein_distance_fast, 3);
}
