# seamCarver

Seam Carver is an implementation of the seam carving content-aware image 
resizing algorithm. This algorithm is intended prepare images for
display on different sized screens by allowing them to be resized without
respect to the aspect ratio of the original. 

This algorithm identifies paths through the image which are considered to 
be of low importance and removes those paths. Removal of a vertical path
reduces the image width by one pixel and likewise for horizontal path. 

More information about the algorithm can be found at 
https://en.wikipedia.org/wiki/Seam_carving

## How it works

The image is turned into a large graph data structures, with weighted
edges connecting each pixel to adjacent pixels, either moving horizontally
or vertical. The edgeweights are calculated by the use of an 'energy
function', which gives pixels a value based on their colour as compared
to neighbouring pixels. 

Shortest paths are then found through the graph, with pixels along those 
paths being removed. The edge weights for the new image are then
recalculated and another seam can be removed. 

## Dependencies

Seam Carver relies on a number of a files in Algs4.jar, which should be included
in the java library when running seamCarver. This library comes with the
textbook: 
    
    Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
      
    Addison-Wesley Professional, 2011, ISBN 0-321-57351-X
      
    http://algs4.cs.princeton.edu 

## usage 

### java SeamCarver

running this command will take the example image - ocean.jpg - and 
remove 50 vertical seams, saving the resultant image as ocean_processed.jpg.

Note: the image is fairly large and the processing takes around 1 minute on 
my machine. Each call to the algorithm itself runs in O(V+E) time, but requires
significant copying of bits memory accesses. 

### use in other classes

To use seamCarver in other classes, you must first create a Picture object 
(Picture is a data structure for handling iamges provided by algs4.jar) and
pass is to the constructor of seamCarver. Then, repeatedly call the
relevant methods.