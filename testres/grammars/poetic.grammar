(* https://esolangs.org/wiki/Poetic_(esolang) *)

S = <ws>? exp*

<exp> = i1 | i2 | i3 | i4 | i5 | i6 | i7 | i8 | i9 | i10 | imore

i1    =     <char> <ws>
i2    =   2 <char> <ws>
i3    =   3 <char> <ws>
i4    =   4 <char> <ws>
i5    =   5 <char> <ws>
i6    =   6 <char> <ws>
i7    =   7 <char> <ws>
i8    =   8 <char> <ws>
i9    =   9 <char> <ws>
i10   =  10 <char> <ws>
imore = 11* char <ws>
char  = #"[A-Za-z0-9\']"
ws    = #"[^A-Za-z0-9\']"+ | EOF