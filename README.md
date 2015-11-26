uasat
=====

This is a SAT based universal algebra calculator framework. Currently it
 does not have any graphical user interface, you need to know java to be
  able to use it. As an example, it has a built in  
[validation](src/org/uasat/math/Validation.java) program, 
which calculates the number of certain structures:

```
A000110 the number of equivalences on a 7 element set is 877.
A000142 the number of permutations on a 7 element set is 5040.
A000372 the number of antichains of 2^4 is 168.
A001035 the number of partial orders on a 5 element set is 4231.
A001710 the number of even permutations on a 7 element set is 2520.
A114714 the number of linear extensions of 2x2x4 is 2452.
The number of essential binary relations on 3 is 462.
Parsing the N5 poset relation and printing its covers: passed.
Finished in 5.73 seconds.
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
You can use problem instances to check whether a concrete boolean
tensor has the given property, or use SAT solvers to find one or all satisfying
boolean assignments. The `core` and `solvers` packages contain the
classes needed to work with tensors and solvers.

The `math` package contains classes that are easier to use than raw tensors.
For example, a binary relation over a 5-element set is expressed by a 5×5
boolean matrix R. This relation is transitive if and only if R·R→R, where 
the matrix multiplication is evaluated using disjunction and conjunction 
(instead of boolean addition and product) and the arrow is the logical 
element-wise implication. Operations, equations and other mathematical
objects and properties can also be expressed using tensors and tensor
operations, and these are captured in this package. You should use these
classes in your program and resort to raw tensor manipulation only when you 
cannot express your problem otherwise.

## License

The code is licensed under the GNU [GPL](LICENSE) version 2 or later. 
Internally, the code uses the SAT4J jar package for sat solving (licensed 
under the LGPL) or produces a DIMACS file which can be parsed and solved 
by external SAT solvers, such as MiniSat.

If you use this code for anything serious in your research, please let me know.
