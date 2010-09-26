#include "ruby.h"

VALUE levenshtein_distance_generic(VALUE self, VALUE rb_o1, VALUE rb_o2, VALUE rb_threshold) {
  int	threshold;
  int	l1, l2;
  int	*prev_row, *curr_row;
  int	col, row;
  int	curr_row_min, result;
  int	offset;

  ID id_length	= rb_intern("length");
  ID id_get	= rb_intern("[]");
  ID id_equal	= rb_intern("==");

  /* Get the sizes of both sequences. */

  l1	= FIX2INT(rb_funcall(rb_o1, id_length, 0));
  l2	= FIX2INT(rb_funcall(rb_o2, id_length, 0));

  /* Convert Ruby's threshold to C's threshold. */

  if (!NIL_P(rb_threshold)) {
    threshold	= FIX2INT(rb_threshold);
  } else {
    threshold	= -1;
  }

  /* Do the expensive calculation on a subset of the sequences, if possible, by removing the common prefix. */

  offset	= 0;
  while RTEST(rb_funcall(rb_funcall(rb_o1, id_get, 1, INT2FIX(offset)), id_equal, 1, rb_funcall(rb_o2, id_get, 1, INT2FIX(offset)))) {
    offset++;
  }

  /* Do the expensive calculation on a subset of the sequences, if possible, by removing the common postfix. */

  while ((l1-1 > offset) && (l2-1 > offset) && RTEST(rb_funcall(rb_funcall(rb_o1, id_get, 1, INT2FIX(l1-1)), id_equal, 1, rb_funcall(rb_o2, id_get, 1, INT2FIX(l2-1))))) {
    l1--;
    l2--;
  }

  l1 -= offset;
  l2 -= offset;

  /* The Levenshtein algorithm itself. */

  /*       s1=              */
  /*       ERIK             */
  /*                        */
  /*      01234             */
  /* s2=V 11234             */
  /*    E 21234             */
  /*    E 32234             */
  /*    N 43334 <- prev_row */
  /*    S 54444 <- curr_row */
  /*    T 65555             */
  /*    R 76566             */
  /*    A 87667             */

  /* Allocate memory for both rows */

  prev_row	= ALLOC_N(int, l1+1);
  curr_row	= ALLOC_N(int, l1+1);

  if ((prev_row == NULL) || (curr_row == NULL)) {
    rb_raise(rb_eNoMemError, "out of memory");
  }

  /* Initialize the current row. */

  for (col=0; col<=l1; col++) {
    curr_row[col]	= col;
  }

  for (row=1; row<=l2; row++) {
    /* Copy the current row to the previous row. */

    memcpy(prev_row, curr_row, sizeof(int)*(l1+1));

    /* Calculate the values of the current row. */

    curr_row[0]		= row;
    curr_row_min	= row;

    for (col=1; col<=l1; col++) {
      /* Equal (cost=0) or substitution (cost=1). */

      curr_row[col]	= prev_row[col-1] + (RTEST(rb_funcall(rb_funcall(rb_o1, id_get, 1, INT2FIX(offset+col-1)), id_equal, 1, rb_funcall(rb_o2, id_get, 1, INT2FIX(offset+row-1)))) ? 0 : 1);

      /* Insertion if it's cheaper than substitution. */

      if (prev_row[col]+1 < curr_row[col]) {
        curr_row[col] = prev_row[col]+1;
      }

      /* Deletion if it's cheaper than substitution. */

      if (curr_row[col-1]+1 < curr_row[col]) {
        curr_row[col] = curr_row[col-1]+1;
      }

      /* Keep track of the minimum value on this row. */

      if (curr_row[col] < curr_row_min) {
        curr_row_min	= curr_row[col];
      }
    }

    /* Return nil as soon as we exceed the threshold. */

    if (threshold > -1 && curr_row_min >= threshold) {
      free(prev_row);
      free(curr_row);

      return Qnil;
    }
  }

  /* The result is the last value on the last row. */

  result	= curr_row[l1];

  free(prev_row);
  free(curr_row);

  /* Return the Ruby version of the result. */

  return INT2FIX(result);
}
