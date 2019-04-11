package finalProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ElectricityConsumption extends Application {
	
	private final FileChooser fileChooser = new FileChooser();
	
	Map<String, Double> consumptionList = new TreeMap<String, Double>();
	
	// array list to store value for finding min and max values
	ArrayList<Double> al = new ArrayList<Double>();
	
	//Make list for selection from combo box
	ObservableList<String> months = 
            FXCollections.observableArrayList("01-Jan", "02-Feb", "03-Mar", "04-Apr", "05-May", "06-Jun",
											"07-Jul", "08-Aug", "09-Sep", "10-Oct", "11-Nov", "12-Dec");
	
	Double sum = 0.0;
	Double average = 0.0;
	Double min = 9999.0;
	Double max = 0.0;
	int count = 0;
	
	//Get the current year from system
	Double currentYear = (double) Calendar.getInstance().get(Calendar.YEAR);
//	Double currentYear = (double) 2017;
	
	// Create Chart
 	final CategoryAxis xAxis = new CategoryAxis(); //String category
 	final NumberAxis yAxis = new NumberAxis();  
 	final BarChart<String, Number> barchart = new BarChart<>(xAxis,yAxis);
 	XYChart.Series<String, Number> series1 = new Series<>();
	
 	// Create Label for summary info
 	Label lbl_min = new Label("Min: ");
 	Label lbl_max = new Label("Max: ");
 	Label lbl_avg = new Label("Average: ");
 	Label lbl_total = new Label("Total: ");
 	
 	
	@Override
	public void start(Stage stage) {
		stage.setTitle("Electricity consumption tracking");
		
		BorderPane mainpanel = new BorderPane();
		
		GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10); //Horizontal space between columns
        grid.setVgap(10); //Vertical space between rows
             	
     	barchart.setTitle("Year " + currentYear.intValue());
		barchart.setLegendVisible(false); //Just one series, legend is not needed
		barchart.setAnimated(false);
		yAxis.setLabel("Consumption (kWh)");
		
		consumptionList.put("year", currentYear);
		
	    resetChart();
	    drawChart();
	    
		grid.add(barchart, 0, 3, 4, 1);
		barchart.getData().add(series1);
		
		// Add Label for summary info
        grid.add(lbl_min, 0, 4);        
        grid.add(lbl_max, 0, 5);        
        grid.add(lbl_avg, 3, 4);        
        grid.add(lbl_total, 3, 5);
        
        // Create Label, textField and button for insertion
        Label lbl_Cons = new Label("Consumption :");
        grid.add(lbl_Cons, 0, 1);
        TextField txtF_Cons = new TextField();
        grid.add(txtF_Cons, 1, 1);
        Label lbl_Month = new Label("Month :");
        grid.add(lbl_Month, 0, 2);
        ComboBox<String> cmb_Month = new ComboBox<String>(months);

        grid.add(cmb_Month, 1, 2);
        Button insert = new Button("Insert");
        grid.add(insert, 2, 1);
        //check if user input value and select month
        insert.disableProperty().bind(
        			txtF_Cons.textProperty().isEmpty()
        		.or(cmb_Month.valueProperty().isNull())
        );
        
        mainpanel.setCenter(grid);
		
		//Create menu bar and File menu
		MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		menuBar.getMenus().add(menuFile);
		
		MenuItem newFile = new MenuItem("New");
		MenuItem open = new MenuItem("Open");
		MenuItem save = new MenuItem("Save");
		MenuItem exit = new MenuItem("Exit");
		menuFile.getItems().addAll(newFile, open, save, new SeparatorMenuItem(),  exit);
		mainpanel.setTop(menuBar);
		
		Scene scene  = new Scene(mainpanel,800,600);
	    stage.setScene(scene);
	    stage.show();
	    
	    //Event handlers
	    insert.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
				    double cons = Double.parseDouble(txtF_Cons.getText());
				    String month = cmb_Month.getValue();
				    consumptionList.put(month, cons);
				    drawChart();
				    getInfo();
				    afterClick();
				} catch (NumberFormatException e) {
					Alert alert = new Alert(AlertType.ERROR);
                	alert.setHeaderText("Check your input!");
                	alert.setContentText("Enter a number value.");
                	afterClick();
                	alert.showAndWait();
				}
				
			}
			public void afterClick() {
				txtF_Cons.clear();
				txtF_Cons.requestFocus();
				cmb_Month.getSelectionModel().clearSelection();	
			}
        });
	    newFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// TODO Auto-generated method stub
				txtF_Cons.clear();
				txtF_Cons.requestFocus();
				cmb_Month.getSelectionModel().clearSelection();
				resetInfo();
				resetChart();
				drawChart();
			}
	    });
	    open.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				 File file = fileChooser.showOpenDialog(stage);
                 if (file != null) {
                    try {
						readConsFromFile(file);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
                 }
			}
		});
	    save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				 File file = fileChooser.showSaveDialog(stage);
                 if (file != null) {
                    saveConsToFile(file);
                 }
			}
		});
	    exit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				//System.exit(0);
				Platform.exit();
			}
		});
	    
	}
	
	private Double getTotal() {	
		sum=0.0;
		consumptionList.values().forEach(value -> {
			sum += value;
		});
		return sum;
	}
	
	private Double getAverage() {
		count = 0;
		for (Double m : consumptionList.values()) {
            if(m != 0) count++;
        }
		average = Math.round(sum / count* 10) / 10.0;	//Get only 1 decimal number
		return average;
	}
	
	private Double getMax() {
		for (Double m : consumptionList.values()) {
            al.add(m);
        }
		for (int i=0; i<al.size();i++) {
            if (al.get(i)> max) {
                max = al.get(i);
            }
        }
        return max;
	}
	
	private Double getMin() {
		for (Double m : consumptionList.values()) {
            al.add(m);
        }
        for (int i=0; i<al.size();i++) {
            if (al.get(i)< min && al.get(i) != 0) {
                min = al.get(i);
            }
        }
        return min;
	}
	//reset Max, Min, Average, Sum values
	private void resetInfo() {
		min = 9999.0;
		max = 0.0;
		average = 0.0;
		count = 0;
		sum=0.0;
		lbl_min.setText("Min: ");
	    lbl_max.setText("Max: ");
	    lbl_total.setText("Total:");
	    lbl_avg.setText("Average: ");
	}
	//Clear all values of previous file and set current values to zero
	private void resetChart() {
		months.forEach(month -> {
	    	 consumptionList.put(month, (double) 0);
	    });
	}
	
	private void drawChart() {
		consumptionList.remove("year");	//make sure the chart contains only months, no more year value once is drawn
		consumptionList.entrySet().forEach((HashMap.Entry<String, Double> entry) -> {
	        String tmpString = entry.getKey();
	        Number tmpValue = entry.getValue();
	        series1.getData().add(new Data<>(tmpString, tmpValue));
	    });
	}
	// get Information on Min, Max, Average, Sum values
	private void getInfo() {
		resetInfo();
		al.clear();
		lbl_min.setText("Min: " + getMin().toString() + " kWh");
	    lbl_max.setText("Max: " + getMax().toString() + " kWh");
	    lbl_total.setText("Total: " + getTotal().toString() + " kWh");
	    lbl_avg.setText("Average: " + getAverage().toString() + " kWh");
	}
	
	//Writes Consumption to file 
	private void saveConsToFile(File file) {
		System.out.println(file.getAbsolutePath());
		consumptionList.put("year", currentYear);	// add year to TreeMap for using when open it later
		System.out.println(consumptionList);
		try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))){
			output.writeObject(consumptionList);
		}
		catch(IOException ex){
			System.out.println("Error: " + ex.getMessage());
		}
	}
	
	//Reads consumption from file
	@SuppressWarnings("unchecked")
	private void readConsFromFile(File file) throws ClassNotFoundException{
		resetChart();
		drawChart();
		try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))){
			consumptionList = (Map<String, Double>) input.readObject();
			System.out.println(consumptionList);
			currentYear = consumptionList.get("year");
			barchart.setTitle("Year " + currentYear.intValue());
			drawChart();
			getInfo();

		}
		catch(IOException ex){
			System.out.println("Error: " + ex.getMessage());
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}