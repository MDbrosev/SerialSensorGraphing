package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.fazecast.jSerialComm.SerialPort;

public class SensorGraph {

    private static SerialPort port;	
    private static int x = 0;
    private JFrame gui;
    private JComboBox<String> ports;
    private JButton connectPort;
    private final JPanel panel;
    private final SerialPort[] getPorts;
    private XYSeries co2;
    private XYSeries tvoc;
    private XYSeries temp;
    private final XYSeriesCollection xysc;
    private final JFreeChart jfchart;
    private String line;
    private String[] nums;
    private Scanner scanner;
    private int number;
    private float number2;
    private double number3;
    private Thread thread;
    
    public SensorGraph(){
      //create the window
      gui = new JFrame();
      gui.setTitle("CCS811 Sensor Readings");
      gui.setSize(600, 400);
      gui.setLayout(new BorderLayout());
      gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
      //create a drop down box and connect button, then place them at the top of the window
      ports = new JComboBox<>();
      connectPort = new JButton("Connect");
      panel = new JPanel();
      panel.add(ports);
      panel.add(connectPort);
      gui.add(panel, BorderLayout.NORTH);
		
      //fill the drop down box
      getPorts = SerialPort.getCommPorts();
      for(int i = 0; i < getPorts.length; i++)
       ports.addItem(getPorts[i].getSystemPortName());
		
      //create the line graph
      co2 = new XYSeries("Carbon Dioxide ppm,");
      tvoc = new XYSeries("Total Volatile Organic Compound ppm,");
      temp = new XYSeries("Temperature F");
      xysc = new XYSeriesCollection(co2);
      xysc.addSeries(tvoc);
      xysc.addSeries(temp);
      jfchart = ChartFactory.createXYLineChart("CCS811 Sensor", "Time (seconds)", "Value", xysc);
      gui.add(new ChartPanel(jfchart), BorderLayout.CENTER);
      		
        //set the connect button and use another thread to listen for data
        connectPort.addActionListener(new ActionListener(){
	    @Override public void actionPerformed(ActionEvent arg0){
	        if(connectPort.getText().equals("Connect")){
	            //connect to the serial port
		    port = SerialPort.getCommPort(ports.getSelectedItem().toString());
		    port.setBaudRate(9600);
                    port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
		    if(port.openPort()){
		        connectPort.setText("Disconnect");
			ports.setEnabled(false);
                    }			
	            //create a new thread that listens for incoming text and populates the graph
		    thread = new Thread(){
		        @Override public void run(){
			  scanner = new Scanner(port.getInputStream());
			    while(scanner.hasNextLine()){
				    try{
					line = scanner.nextLine();
                                        nums = line.split(",");
				        number = Integer.parseInt(nums[0]);
				        number2 = Float.parseFloat(nums[1]);
				        number3 = Double.parseDouble(nums[2]);
					temp.add(x, number3);
                                        tvoc.add(x++, number2);
                                        co2.add(x++, number);
					gui.repaint();
			            }catch(Exception e) {}
			    }
					scanner.close();
			}
		    };
                    thread.start();
	        }else{
		    //disconnect from the serial port
	            port.closePort();
		    ports.setEnabled(true);
		    connectPort.setText("Connect");
		    co2.clear();
		    tvoc.clear();
		    temp.clear();
		    x = 0;
		}
	    }
        });
        //show the window
	gui.setVisible(true);
    }
    
    public static void main(String[] args) {
      SensorGraph graph = new SensorGraph();	
    }
}
