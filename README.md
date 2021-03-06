uasat
=====

This is a SAT based universal algebra calculator framework.
Currently it does not have any graphical user interface,
you need to know java to be able to use it. As an example,
it has a built in [validation](src/org/uasat/math/Validation.java)
program, which calculates the number of certain structures from
the [on-line encyclopedia of integer sequences](http://oeis.org/):

```
A000001 the number of groups of order 4 is 2.
A000110 the number of equivalences on a 7-element set is 877.
A000273 the number of non-isomorphic 4-element digraphs is 218.
A000372 the number of antichains of 2^4 is 168.
A001035 the number of partial orders on a 5-element set is 4231.
A001329 the number of non-isomorphic 3-element groupoids is 3330.
A001710 the number of even permutations on a 7-element set is 2520.
A002720 the number of partial permutations on 5 is 1546.
A002860 the number of labelled quasigroups of order 4 is 576.
A006117 the number of subspaces of Z_3^4 is 212.
A023815 the number of commutative semigroups on 4 is 1140.
A084279 the number of 3-colorable 5-element simple graphs is 958.
A114714 the number of linear extensions of 2x2x3 is 2452.
Total variables: 16340, clauses: 71445.
Finished in 1.98 seconds.
```

I have also included a few small programs used for my research, you can
disregard those.

## Internal structure

Every mathematical object is represented internally by a boolean tensor
(multidimensional matrix). The entries in these tensors are either
concrete boolean values or boolean terms. You can use tensor operations
(reshaping, contractions, folds, maps, etc.) to create more complicated
tensors and boolean values. A problem is expressed as a function that
takes some boolean tensors as input and calculates a boolean value as
output, in essence it is just a huge boolean formula.
You can use that formula to check whether concrete boolean
tensors have the given property, or use SAT solvers to find one or all satisfying
boolean assignments. The [org.uasat.core](src/org/uasat/core) and
[org.uasat.solvers](src/org/uasat/solvers) packages contain the
classes needed to work with tensors and solvers.

The [org.uasat.math](src/org/uasat/math) package contains classes that are
easier to use than raw tensors.
For example, a binary relation over a 5-element set is expressed by a 5×5
boolean matrix R. This relation is transitive if and only if R·R ≤ R, where
the matrix multiplication is evaluated using disjunction and conjunction
(instead of boolean addition and multiplication) and ≤ is the logical
element-wise implication. Operations, equations and other mathematical
objects and properties can also be expressed using tensors and tensor
operations, and these are captured in this package. You should use these
classes in your program and resort to raw tensor manipulation only when you
cannot express your problem otherwise.

## Build

You can use the enclosed [build.xml](build.xml) ant makefile file
to create the `uasat.jar` file: just run the `ant` or `ant jar` command line programs
in the project directory. You can also link your program against this jar file,
or fork this project for your research.

## License

The code is licensed under the GNU [GPL](LICENSE) version 2 or later.
Internally, the code uses the pure java SAT4J package for SAT solving
(licensed under the LGPL) or produces a DIMACS file which can be parsed
and solved by external SAT solvers, such as MiniSat.

If you use this code for anything serious in your research, please let
[me](http://www.math.u-szeged.hu/~mmaroti/) know.
