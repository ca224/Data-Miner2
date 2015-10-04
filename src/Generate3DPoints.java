import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

//Note, this program was taken from the CS634 moodle board

public class Generate3DPoints {
	public static int TARGET_CLUSTERS = 3; // number of clusters we are
											// targeting for the various
											// "schemes"
	public static int NUMBER_OF_POINTS = 500; // number of points we want
	public static float SPACE_BETWEEN = 50; // space between clusters for scheme
											// 1

	public static void PrintUsage() {
		System.out.println("USAGE: java Generate3DPoints SCHEME#\n");
		System.out
				.println("Scheme #1: Separating points into TARGET_CLUSTERS with SPACE_BETWEEN between them");
		System.out
				.println("Scheme #2: Generating each cluster into different overlapping bands, the first one going 0 to 1/4 the second 0 to 1/2 for 4 clusters, etc.... ");
		System.out
				.println("Scheme #3: No restriction, every point is generated anywhere within the x/y/z max");
		return;
	}

	public static void main(String[] args) {
		// Change these numbers to move the cluster around
		int xMax = 1000;
		int yMax = 1000;
		int zMax = 1000;

		PrintUsage();
		Scanner scanner = new Scanner(System.in);
		int input = scanner.nextInt();

		// generateNormalPoints(xMax, 0, yMax, 0, zMax, 0, 100);
		generateRandomPoints(xMax, yMax, zMax, NUMBER_OF_POINTS, input);
	}

	/*
	 * Created by me to use the normal random generator, modified with ideas
	 * from Jianjun Huang however I added the concept of space between clusters
	 */
	public static void generateRandomPoints(int xMax, int yMax, int zMax,
			int numberOfPoints, int scheme) {
		File file = new File("C:/data2.txt");
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter outFile = new PrintWriter(bw);
		Random r = new Random();
		int clustergroup;
		int intervalX = (int) (xMax / TARGET_CLUSTERS);
		int intervalY = (int) (yMax / TARGET_CLUSTERS);
		int intervalZ = (int) (zMax / TARGET_CLUSTERS);
		int x, y, z;
		for (int i = 0; i < numberOfPoints; i++) {
			clustergroup = i / (numberOfPoints / TARGET_CLUSTERS);
			if (scheme == 1) { // Separated clusters
				x = Math.round(intervalX * clustergroup + SPACE_BETWEEN
						* clustergroup)
						+ r.nextInt(intervalX);
				y = Math.round(intervalY * clustergroup + SPACE_BETWEEN
						* clustergroup)
						+ r.nextInt(intervalY);
				z = Math.round(intervalZ * clustergroup + SPACE_BETWEEN
						* clustergroup)
						+ r.nextInt(intervalZ);
			} else if (scheme == 2) { // Overlapping bands
				clustergroup++;
				x = r.nextInt(intervalX * clustergroup);
				y = r.nextInt(intervalY * clustergroup);
				z = r.nextInt(intervalZ * clustergroup);
			} else { // no restriction on any points
				x = r.nextInt(xMax);
				y = r.nextInt(yMax);
				z = r.nextInt(zMax);
			}

			System.out.printf("%3d,%3d,%3d%n", x, y, z);
			outFile.printf("%3d,%3d,%3d%n", x, y, z);
		}
		outFile.flush();
		outFile.close();
	}

	/**
	 * This method will output a random, normal vector in 3D space around a 0,
	 * 0, 0 center The Max and Min specifed are only a loose limitation bacuse
	 * the values can exceed them
	 */
	public static void generateNormalPoints(int xMax, int xMin, int yMax,
			int yMin, int zMax, int zMin, int numberOfPoints) {
		File file = new File("C:/data2.txt");
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter outFile = new PrintWriter(bw);
		Random r = new Random();

		for (int i = 0; i < numberOfPoints; i++) {
			int x = (int) (r.nextGaussian() * (xMax - xMin)) + xMin;
			int y = (int) (r.nextGaussian() * (yMax - yMin)) + yMin;
			int z = (int) (r.nextGaussian() * (zMax - zMin)) + zMin;

			System.out.printf("%3d,%3d,%3d%n", x, y, z);
			outFile.printf("%3d,%3d,%3d%n", x, y, z);
		}
		outFile.flush();
		outFile.close();
	}
}