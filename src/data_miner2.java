import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class data_miner2 {
	
	static int Point_num = 500;
	//The array used to store all points' coordinate
	static double[][] Points = new double[Point_num][3];
	//The matrix used to store all distance between any 2 points
	static double[][] Dis_matrix = new double[Point_num][Point_num];
	static List<Integer> Outlier = new ArrayList<Integer>();
	static List<List<Integer>> Finallist_near = new ArrayList<List<Integer>>();
	static List<List<Integer>> Finallist_far = new ArrayList<List<Integer>>();
	static List<List<Integer>> Finallist_average = new ArrayList<List<Integer>>();
	static List<List<Integer>> Finallist_center = new ArrayList<List<Integer>>();

	public static void main(String[] args) {
		System.out.println("Please input data file path: ");
		Scanner scanner = new Scanner(System.in);
		String filepath = scanner.nextLine();
		System.out.println("Please input outlier distence: ");
		scanner = new Scanner(System.in);
		int out_dis = scanner.nextInt();
		System.out.println("Please input outlier percentage(%): ");
		scanner = new Scanner(System.in);
		int out_factor = scanner.nextInt();
		System.out.println("Please input clauster num: ");
		scanner = new Scanner(System.in);
		int cluster_num = scanner.nextInt();
		//I/O process
		File file = new File(filepath);
		InputStreamReader isr = null;
		BufferedReader in = null;
		String thisline;
		String[] part;
		try {
			FileInputStream fis = new FileInputStream(file.getAbsoluteFile());
			isr = new InputStreamReader(fis, "UTF-8");
			in = new BufferedReader(isr);
			int i = 0;
			//Read points' coordinate from file
			while ((thisline = in.readLine()) != null) {
				part = thisline.split(",");
				double[] intpart = new double[3];
				for (int j = 0; j < part.length; j++) {
					intpart[j] = Double.parseDouble(part[j].trim());
				}
				//The "i" is like the ID of this point
				Points[i] = intpart;
				i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Outlier = FindOutlier(out_factor, out_dis);

		Finallist_near = RemoveOutlier(Outlier);
		Finallist_far = RemoveOutlier(Outlier);
		Finallist_average = RemoveOutlier(Outlier);
		Finallist_center = RemoveOutlier(Outlier);
		Cluster_nearest(cluster_num);
		Cluster_furthest(cluster_num);
		Cluster_average(cluster_num);
		Cluster_center(cluster_num);
		double sc_near = Silhouette_Coefficient(Finallist_near);
		double sc_far = Silhouette_Coefficient(Finallist_far);
		double sc_average = Silhouette_Coefficient(Finallist_average);
		double sc_center = Silhouette_Coefficient(Finallist_center);
		System.out.println("sc_near: " + sc_near + "; sc_far: " + sc_far
				+ "; sc_average: " + sc_average + "; sc_center: " + sc_center);
		double sc_min = Math.min(
				Math.min(Math.min(1 - sc_far, 1 - sc_near), 1 - sc_average),
				1 - sc_center);
		if (sc_min == 1-sc_near) {
			System.out.println("the distance between the nearest two points in the two clusters is the best!");
			PrintFinal(Finallist_near);
		} else if (sc_min == 1-sc_far) {
			System.out.println("the distance between the farthest two points in the two clusters is the best!");
			PrintFinal(Finallist_far);
		} else if (sc_min == 1-sc_average) {
			System.out.println("the average distance between points in the two clusters is the best!");
			PrintFinal(Finallist_average);
		} else {
			System.out.println("the distance between the centers of the two clusters is the best");
			PrintFinal(Finallist_center);
		}
		/**
		System.out.println("near!!");
		PrintFinal(Finallist_near);
		System.out.println("far!!");
		PrintFinal(Finallist_far);
		System.out.println("Average!!");
		PrintFinal(Finallist_average);
		System.out.println("center!!");
		PrintFinal(Finallist_center);
		*/
	}

	private static List<Integer> FindOutlier(int factor, int dis) {
		List<Integer> outlier = new ArrayList<Integer>();
		for (int i = 0; i < Points.length; i++) {
			double count = 0;
			for (int j = 0; j < Points.length; j++) {
				if (i == j)
					Dis_matrix[i][j] = 0;
				if (i != j) {
					//Populate the distance matrix
					double distence = CalDistence(Points[i], Points[j]);
					Dis_matrix[i][j] = distence;
					Dis_matrix[j][i] = distence;
					if (distence >= dis)
						count++;
				}
			}
			if (count / (Point_num - 1) >= ((double) factor) / 100) {
				outlier.add(i);
			}
		}
		return outlier;
	}

	private static List<List<Integer>> RemoveOutlier(List<Integer> outlier) {
		List<List<Integer>> clean = new ArrayList<List<Integer>>();
		for (int i = 0; i < Points.length; i++) {
			int flag = 0;
			for (int j = 0; j < outlier.size(); j++) {
				if (i == outlier.get(j).intValue())
					flag = 1;
			}
			if (flag == 0) {
				List<Integer> tmp = new ArrayList<Integer>();
				tmp.add(i);
				clean.add(tmp);
			}
		}
		// System.out.println(clean.size());
		return clean;
	}

	private static void PrintFinal(List<List<Integer>> finallist) {
		for (int i = 0; i < finallist.size(); i++) {
			System.out.println();
			System.out.println("cluster: " + (i + 1) + " , Total "
					+ finallist.get(i).size() + " Points!");
			for (Integer a : finallist.get(i)) {
				for (int j = 0; j < Points[a].length; j++) {
					System.out.print((int) Points[a][j] + ",");
				}
				System.out.println();
			}
		}
		System.out.println();
		System.out.println("Outlier: Total " +Outlier.size()+ " Points!");
		for (Integer a : Outlier) {
			for (int j = 0; j < Points[a].length; j++) {
				System.out.print((int)Points[a][j] + ",");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Done!");
	}

	private static double CalDistence(double[] point1, double[] point2) {
		double distence;
		distence = Math.sqrt(Math.pow(point1[0] - point2[0], 2)
				+ Math.pow(point1[1] - point2[1], 2)
				+ Math.pow(point1[2] - point2[2], 2));
		return distence;
	}

	private static double Silhouette_Coefficient(List<List<Integer>> finallist) {
		double sc_sum = 0;
		for (int i = 0; i < finallist.size(); i++) {
			for (int j = 0; j < finallist.get(i).size(); j++) {
				double sc_point = 0;
				double inner_dis = 0;
				double b1 = 0;
				int firstcheck = 0;
				for (int k = 0; k < finallist.size(); k++) {
					double outer_dis = 0;
					for (int m = 0; m < finallist.get(k).size(); m++) {
						double check = Dis_matrix[finallist.get(i).get(j)][finallist
								.get(k).get(m)];
						if (i == k) {
							inner_dis += check;
						} else {
							outer_dis += check;
						}
					}
					if (i != k) {
						outer_dis = outer_dis / finallist.get(k).size();
						if (firstcheck == 0) {
							b1 = outer_dis;
							firstcheck = 1;
						} else {
							if (b1 > outer_dis)
								b1 = outer_dis;
						}
					}
				}
				double a1 = 0;
				if (inner_dis != 0)
					a1 = inner_dis / (finallist.get(i).size() - 1);
				sc_point = (b1 - a1) / Math.max(a1, b1);
				sc_sum += sc_point;
			}
		}
		return sc_sum / RemoveOutlier(Outlier).size();
	}

	private static void Cluster_center(int cluster_num) {
		while (Finallist_center.size() > cluster_num) {
			int firstcheck = 0;
			double min = 0;
			int min_i = 0, min_k = 0;
			for (int i = 0; i < Finallist_center.size() - 1; i++) {
				double[] p1 = new double[3];
				for (int j = 0; j < Finallist_center.get(i).size(); j++) {
					p1[0] += Points[Finallist_center.get(i).get(j)][0];
					p1[1] += Points[Finallist_center.get(i).get(j)][1];
					p1[2] += Points[Finallist_center.get(i).get(j)][2];
				}
				p1[0] = p1[0] / Finallist_center.get(i).size();
				p1[1] = p1[1] / Finallist_center.get(i).size();
				p1[2] = p1[2] / Finallist_center.get(i).size();
				for (int k = i + 1; k < Finallist_center.size(); k++) {
					double[] p2 = new double[3];
					for (int m = 0; m < Finallist_center.get(k).size(); m++) {
						p2[0] += Points[Finallist_center.get(k).get(m)][0];
						p2[1] += Points[Finallist_center.get(k).get(m)][1];
						p2[2] += Points[Finallist_center.get(k).get(m)][2];
					}
					p2[0] = p2[0] / Finallist_center.get(k).size();
					p2[1] = p2[1] / Finallist_center.get(k).size();
					p2[2] = p2[2] / Finallist_center.get(k).size();
					double dis = CalDistence(p1, p2);
					if (firstcheck == 0) {
						min = dis;
						min_i = i;
						min_k = k;
						firstcheck++;
					} else {
						if (min > dis) {
							min = dis;
							min_i = i;
							min_k = k;
						}
					}
				}
			}
			Finallist_center.get(min_i).addAll(Finallist_center.get(min_k));
			Finallist_center.remove(min_k);
		}
	}

	private static void Cluster_average(int cluster_num) {
		while (Finallist_average.size() > cluster_num) {
			int firstcheck = 0;
			double min = 0;
			int min_i = 0, min_k = 0;
			for (int i = 0; i < Finallist_average.size() - 1; i++) {
				for (int k = i + 1; k < Finallist_average.size(); k++) {
					double check = 0;
					for (int j = 0; j < Finallist_average.get(i).size(); j++) {
						for (int m = 0; m < Finallist_average.get(k).size(); m++) {
							check += Dis_matrix[Finallist_average.get(i).get(j)][Finallist_average
									.get(k).get(m)];
						}
					}
					int denominator = Finallist_average.get(i).size()
							* Finallist_average.get(k).size();
					check = check / denominator;
					if (firstcheck == 0) {
						min = check;
						min_i = i;
						min_k = k;
						firstcheck++;
					} else {
						if (min > check) {
							min = check;
							min_i = i;
							min_k = k;
						}
					}
				}
			}
			Finallist_average.get(min_i).addAll(Finallist_average.get(min_k));
			Finallist_average.remove(min_k);
		}
	}

	private static void Cluster_furthest(int cluster_num) {
		while (Finallist_far.size() > cluster_num) {
			double check = 0;
			int firstcheck = 0;
			double min = 0;
			int min_i = 0, min_k = 0;
			for (int i = 0; i < Finallist_far.size() - 1; i++) {
				for (int k = i + 1; k < Finallist_far.size(); k++) {
					double cluster_max = 0;
					for (int j = 0; j < Finallist_far.get(i).size(); j++) {
						for (int m = 0; m < Finallist_far.get(k).size(); m++) {
							check = Dis_matrix[Finallist_far.get(i).get(j)][Finallist_far
									.get(k).get(m)];
							if (check > cluster_max)
								cluster_max = check;
						}
					}
					if (firstcheck == 0) {
						min = cluster_max;
						min_i = i;
						min_k = k;
						firstcheck++;
					} else {
						if (min > cluster_max) {
							min = cluster_max;
							min_i = i;
							min_k = k;
						}
					}
				}
			}
			Finallist_far.get(min_i).addAll(Finallist_far.get(min_k));
			Finallist_far.remove(min_k);
		}
	}

	private static void Cluster_nearest(int cluster_num) {
		while (Finallist_near.size() > cluster_num) {
			int firstcheck = 0;
			double min = 0;
			int min_i = 0, min_k = 0;
			for (int i = 0; i < Finallist_near.size() - 1; i++) {
				for (int j = 0; j < Finallist_near.get(i).size(); j++) {
					for (int k = i + 1; k < Finallist_near.size(); k++) {
						for (int m = 0; m < Finallist_near.get(k).size(); m++) {
							double check = Dis_matrix[Finallist_near.get(i)
									.get(j)][Finallist_near.get(k).get(m)];
							if (firstcheck == 0) {
								min = check;
								min_i = i;
								min_k = k;
								firstcheck++;
							} else {
								if (min > check) {
									min = check;
									min_i = i;
									min_k = k;
								}
							}
						}
					}
				}
			}
			Finallist_near.get(min_i).addAll(Finallist_near.get(min_k));
			Finallist_near.remove(min_k);
		}
	}
}
