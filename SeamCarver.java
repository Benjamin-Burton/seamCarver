import edu.princeton.cs.algs4.Picture;
import java.awt.Color;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.DijkstraSP;

public class SeamCarver {

    private Picture picture;
    private int width;
    private int height;
    private double[][] energies;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException();

        this.picture = new Picture(picture);
        this.width = picture.width();
        this.height = picture.height();
        // calculate energy functions - should only happen once per picture
        energies = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                energies[x][y] = setEnergy(x, y);
            }
        }

        // for (int y = 0; y < height; y++) {
        //     for (int x = 0; x < width; x++) {
        //         System.out.print(energies[x][y] + " ");
        //     }
        //     System.out.println();
        // }
        // System.out.println("\n--------------------");
    }

    private int getVFromxy(int x, int y) {
        return y * width + x;
    }

    private int getXFromV(int v) {
        return v % width;
    }

    private int getYFromV(int v) {
        return v / width;
    }
 
    // current picture
    public Picture picture() {
        return new Picture(this.picture);
    }
 
    // width of current picture
    public int width() {
        return picture.width();
    }
 
    // height of current picture
    public int height() {
        return picture.height();
    }

    private double setEnergy(int x, int y) {
        if (x < 0 || x > picture.width() - 1) throw new IllegalArgumentException();
        if (y < 0 || y > picture.height() - 1) throw new IllegalArgumentException();

        // account for edge pixels which always return 1000
        if (x == picture.width() - 1 ||
            x == 0 ||
            y == picture.height() - 1 ||
            y == 0) {
                return 1000;
            }

        Color xPlusOneColor = picture.get(x + 1, y);
        Color xMinusOneColor = picture.get(x - 1, y);
        Color yPlusOneColor = picture.get(x, y + 1);
        Color yMinusOneColor = picture.get(x, y - 1);

        int rx = xPlusOneColor.getRed() - xMinusOneColor.getRed();
        int gx = xPlusOneColor.getGreen() - xMinusOneColor.getGreen();
        int bx = xPlusOneColor.getBlue() - xMinusOneColor.getBlue();
        double gradx = rx * rx + gx * gx + bx * bx;

        int ry = yPlusOneColor.getRed() - yMinusOneColor.getRed();
        int gy = yPlusOneColor.getGreen() - yMinusOneColor.getGreen();
        int by = yPlusOneColor.getBlue() - yMinusOneColor.getBlue();
        double grady = ry * ry + gy * gy + by * by;
        // System.out.println(gradx + grady);

        return Math.sqrt(gradx + grady);
    }
 
    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > picture.width() - 1) throw new IllegalArgumentException();
        if (y < 0 || y > picture.height() - 1) throw new IllegalArgumentException();
        return energies[x][y];
    }
 
    // sequence of indices for horizontal seam
    public int[] findVerticalSeam() {
        // create vertical-oriented graph
        width = picture.width();
        height = picture.height();
        int vertSource = width * height;
        int vertSink = width * height + 1;

        EdgeWeightedDigraph gVert = new EdgeWeightedDigraph((width * height) + 2);

        // vertical seams

        // let vertSource vertex be second last vertex
        // let vertSink vertex be last vertex
        // connect up vertSource to top row and vertSink to bottom row
        for (int i = 0; i < picture.width(); i++) {
            gVert.addEdge(new DirectedEdge(vertSource, i, 1000));
            gVert.addEdge(new DirectedEdge(getVFromxy(i, height - 1), vertSink, 0));
            if (height > 1 && i > 0 && i < picture.width() - 1) {
                gVert.addEdge(new DirectedEdge(getVFromxy(i - 1, height - 2), getVFromxy(i, height - 1), 1000));
                gVert.addEdge(new DirectedEdge(getVFromxy(i, height - 2), getVFromxy(i, height - 1), 1000));
                gVert.addEdge(new DirectedEdge(getVFromxy(i + 1, height - 2), getVFromxy(i, height - 1), 1000));
            }   
        }

        // connect up edges to all be 1000 and create attachments to them
        for (int i = 1; i < height; i++) {
            // left side
            gVert.addEdge(new DirectedEdge(getVFromxy(0, i - 1), getVFromxy(0, i), 1000));
            if (width > 1) {
                gVert.addEdge(new DirectedEdge(getVFromxy(1, i - 1), getVFromxy(0, i), 1000));
            }
            
            // right side
            gVert.addEdge(new DirectedEdge(getVFromxy(width - 1, i - 1), getVFromxy(width - 1, i), 1000));
            if (width > 1) {
                gVert.addEdge(new DirectedEdge(getVFromxy(width - 2, i - 1), getVFromxy(width - 1, i), 1000));
            }    
        }

        // calculate the energy function for each internal pixel and add it to the graphs
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int vertex = getVFromxy(x, y);
                gVert.addEdge(new DirectedEdge(getVFromxy(x - 1, y - 1), vertex, energies[x][y]));
                gVert.addEdge(new DirectedEdge(getVFromxy(x, y - 1), vertex, energies[x][y]));
                gVert.addEdge(new DirectedEdge(getVFromxy(x + 1, y - 1), vertex, energies[x][y]));
            }
        }
        
        DijkstraSP shortPath = new DijkstraSP(gVert, vertSource);
        Iterable<DirectedEdge> sp = shortPath.pathTo(vertSink);
        int[] res = new int[height];
        int i = 0;
        for (DirectedEdge e: sp) {
            res[i] = getXFromV(e.to());
            i++;
            if (i == height) break;
        }
        return res;
    }
 
    // sequence of indices for vertical seam
    public int[] findHorizontalSeam() {
        // create horizontal-oriented graph
        width = picture.width();
        height = picture.height();
        int horiSource = width * height;
        int horiSink = width * height + 1;

        EdgeWeightedDigraph gHori = new EdgeWeightedDigraph((width * height) + 2);
        
        // horizontal seams
        // let horiSource vertex be second last vertex
        // let horiSink vertex be last vertex
        // connect up horiSource to left column and horiSink to right column
        for (int i = 0; i < picture.height(); i++) {
            gHori.addEdge(new DirectedEdge(horiSource, i * width, 1000));
            gHori.addEdge(new DirectedEdge(getVFromxy(width - 1, i), horiSink, 0));
            if (width > 1 && i > 0 && i < picture.height() - 1) {
                gHori.addEdge(new DirectedEdge(getVFromxy(width - 2, i - 1), getVFromxy(width - 1, i), 1000));
                gHori.addEdge(new DirectedEdge(getVFromxy(width - 2, i), getVFromxy(width - 1, i), 1000));
                gHori.addEdge(new DirectedEdge(getVFromxy(width - 2, i + 1), getVFromxy(width - 1, i), 1000));
            }
        }

        // connect up top and bottom edges to all be 1000 and create attachments to them
        for (int i = 1; i < width; i++) {
            // top side
            gHori.addEdge(new DirectedEdge(getVFromxy(i - 1, 0), getVFromxy(i, 0), 1000));
            if (height > 1) {
                gHori.addEdge(new DirectedEdge(getVFromxy(i - 1, 1), getVFromxy(i, 0), 1000));
            }
            
            // bottom side
            gHori.addEdge(new DirectedEdge(getVFromxy(i - 1, height - 1), getVFromxy(i, height - 1), 1000));
            if (height > 1) {
                gHori.addEdge(new DirectedEdge(getVFromxy(i - 1, height - 2), getVFromxy(i, height - 2), 1000));
            }    
        }
        
        // calculate the energy function for each internal pixel and add it to the graphs
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int vertex = getVFromxy(x, y);
                gHori.addEdge(new DirectedEdge(getVFromxy(x - 1, y - 1), vertex, energies[x][y]));
                gHori.addEdge(new DirectedEdge(getVFromxy(x - 1, y), vertex, energies[x][y]));
                gHori.addEdge(new DirectedEdge(getVFromxy(x - 1, y + 1), vertex, energies[x][y]));
            }
        }
        
        DijkstraSP shortPath = new DijkstraSP(gHori, horiSource);
        Iterable<DirectedEdge> sp = shortPath.pathTo(horiSink);
        int [] res = new int[width];
        int i = 0;
        for (DirectedEdge e: sp) {
            res[i] = getYFromV(e.to());
            i++;
            if (i == width) break;
        }
        return res;
    }
 
    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) throw new IllegalArgumentException();
        if (seam.length != width) throw new IllegalArgumentException();
        if (width < 1) throw new IllegalArgumentException();
        if (height <= 1) throw new IllegalArgumentException();

        for (int i = 1; i < seam.length; i++) {
            if (Math.abs(seam[i] - seam[i - 1]) > 1) {
                throw new IllegalArgumentException();
            }
            if (seam[i] < 0 || seam[i] > height - 1) {
                throw new IllegalArgumentException();
            }
        }

        Picture newP = new Picture(width, height - 1);
        // search one column at a time and copy pixels and energies across
        for (int x = 0; x < width; x++) { // x = col
            int skip = seam[x];
            for (int y = 0; y < skip; y++) { // y = row
                newP.setRGB(x, y, picture.getRGB(x, y));
            }
            for (int y = skip + 1; y < height; y++) {
                newP.setRGB(x, y - 1, picture.getRGB(x, y));
            }
        }

        this.picture = newP;
        this.width = newP.width();
        this.height = newP.height();
        // update energies
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                energies[x][y] = setEnergy(x, y);
            }
        }
        // make sure edges of energies are 1000

        for (int y = 0; y < height; y++) {
            energies[0][y] = 1000;
            energies[width - 1][y] = 1000;
        }
        for (int x = 0; x < width; x++) {
            energies[x][0] = 1000;
            energies[x][height - 1] = 1000;
        }
        // for (int y = 0; y < height; y++) {
        //     for (int x = 0; x < width; x++) {
        //         System.out.print(energies[x][y] + " ");
        //     }
        //     System.out.println();
        // }
        // System.out.println("\n--------------------");
    }
 
    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) throw new IllegalArgumentException();
        if (seam.length != height) throw new IllegalArgumentException();
        if (height < 1) throw new IllegalArgumentException();
        if (width <= 1) throw new IllegalArgumentException();

        for (int i = 1; i < seam.length; i++) {
            if (Math.abs(seam[i] - seam[i - 1]) > 1) {
                throw new IllegalArgumentException();
            }
            if (seam[i] < 0 || seam[i] > width - 1) {
                throw new IllegalArgumentException();
            }
        }

        Picture newP = new Picture(width - 1, height);
        // search one row at a time and copy pixels and energies across
        for (int y = 0; y < height; y++) {
            int skip = seam[y];
            for (int x = 0; x < skip; x++) {
                newP.setRGB(x, y, picture.getRGB(x, y));
            }
            for (int x = skip + 1; x < width; x++) {
                newP.setRGB(x - 1, y, picture.getRGB(x, y));
            }
        }

        picture = newP;
        this.width = newP.width();
        this.height = newP.height();
        // update energies
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                energies[x][y] = setEnergy(x, y);
            }
        }
        // make sure edges of energies are 1000
        for (int y = 0; y < height; y++) {
            energies[0][y] = 1000;
            energies[width - 1][y] = 1000;
        }
        for (int x = 0; x < width; x++) {
            energies[x][0] = 1000;
            energies[x][height - 1] = 1000;
        }
        // for (int y = 0; y < height; y++) {
        //     for (int x = 0; x < width; x++) {
        //         System.out.print(energies[x][y] + " ");
        //     }
        //     System.out.println();
        // }
        // System.out.println("\n--------------------");
    }
 
    //  unit testing (optional)
    public static void main(String[] args) {
        // Picture p = new Picture("6x5.png");
        // SeamCarver s = new SeamCarver(p);
        
        // s.removeHorizontalSeam(s.findHorizontalSeam());
        // s.removeVerticalSeam(s.findVerticalSeam());
        // s.removeVerticalSeam(s.findVerticalSeam());
        // s.removeVerticalSeam(s.findVerticalSeam());
        // s.removeVerticalSeam(s.findVerticalSeam());
        
        Picture pTest = new Picture(6, 6);
        String hex = "#050003 #020808 #090104 #080403 #050301 #000104 #020807 #000706 #010801 #050608 #050007 #000605 #070100 #040403 #040400 #010607 #020002 #080809 #080609 #070906 #060308 #070004 #080705 #080602 #080405 #020407 #000501 #070207 #010804 #060306 #080304 #080907 #030005 #070103 #050103 #000102";
        hex.replace("\n", "");
        String[] hexnums = hex.split(" ");
        int count = 0;
        for (String hexnum: hexnums) {
            int col = count % 6;
            int row = count / 6;
            // System.out.println(col + " " + row);
            int red = Integer.parseInt(hexnum.substring(1,3), 16);
            int green = Integer.parseInt(hexnum.substring(3,5), 16);
            int blue = Integer.parseInt(hexnum.substring(5,7), 16);
            // System.out.println(red + green + blue);
            pTest.set(col, row, new Color(red, green, blue));
            count++;
        }

        SeamCarver sTest = new SeamCarver(pTest);
        sTest.removeVerticalSeam(new int[] {3, 4, 3, 3, 3, 2});
        // int[] problemSeam = sTest.findVerticalSeam();

        System.out.println(sTest.height() + "  " + sTest.width());

        // Picture p2 = new Picture("3x4.png");
        // System.out.print(p2.height());
        // System.out.print(p2.height());

        Picture p3 = new Picture("tennis.jpg");
        p3.show();

        SeamCarver s4 = new SeamCarver(p3);
        System.out.println(p3.height());
        System.out.println(s4.findVerticalSeam().length);
        s4.removeVerticalSeam(s4.findVerticalSeam());
        
        for (int i = 0; i < 50; i++) {
            s4.removeVerticalSeam(s4.findVerticalSeam());
            s4.removeHorizontalSeam(s4.findHorizontalSeam());
            s4.removeHorizontalSeam(s4.findHorizontalSeam());
        }
        s4.picture().show();
    }
 }