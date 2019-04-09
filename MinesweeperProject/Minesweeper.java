import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
 * Richard Vezeau
 * 300281766
 * 
 * to-do:
 * [X] misflagged spots are revealed when mine is tripped
 * [X] left clicking revealed spot with flags shows all spots around
 * [X] first click is always spot with 0 mines
 * [X] only clickable thing after win/loss is smily face
 * [X] difficulty menu
 * [X] timer
 * [X] border around mines
 * [ ] highscores
 * 
 * bugs:
 * - When you try to select a difficulty BEFORE clicking at least once, it crashes
 */

public class Minesweeper extends Application {
	int finalDifficulty = 1;
	int seconds = 0;
	Integer oneSec = 0;
	Integer tenSec = 0;
	Integer hunSec = 0;
	Timeline timer;
	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage theStage) {


		GameStats stats = new GameStats(finalDifficulty); //bombs, gridSize
		int bangBangs = stats.getBombs(); 
		int gridSize = stats.getGrid(); 


		BombsLeftCounter bombsLeft = new BombsLeftCounter(bangBangs);

		GridPane MSGrid = new GridPane();
		GridPane topGrid = new GridPane();
		BorderPane outerPane = new BorderPane();
		GridPane bombsLeftCounter = new GridPane();
		GridPane time = new GridPane();
		BorderPane fullPane = new BorderPane();


		Menu difficulty = new Menu("Difficulty");
		MenuItem beg = new MenuItem("Begginer");
		MenuItem med = new MenuItem("Intermediate");
		MenuItem high = new MenuItem("Expert");
		difficulty.getItems().add(beg);
		difficulty.getItems().add(med);
		difficulty.getItems().add(high);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(difficulty);
		VBox vBox = new VBox(menuBar);

		bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() / 10) + ".png" ,20,50,true,true))), 0, 0);
		bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() % 10) + ".png",20,50,true,true))), 1, 0);
		time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + hunSec.intValue() + ".png",20,50,true,true))), 0, 0);
		time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + tenSec.intValue() + ".png",20,50,true,true))), 1, 0);
		time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + oneSec.intValue() + ".png",20,50,true,true))), 2, 0);
		

		topGrid.add(bombsLeftCounter, 0, 0);
		topGrid.add(time, 2, 0);
		topGrid.setAlignment(Pos.CENTER);
		topGrid.setHgap(50);

		int[][] buttonValue = new int[gridSize][gridSize];
		MSButton buttons[][] = new MSButton[gridSize][gridSize];
		SmilyButton smily = new SmilyButton();
		

		for(int row = 0; row < gridSize; row++) {
			for(int col = 0; col < gridSize; col++) {
				double buttonSize = 1;
				if(finalDifficulty == 1) {
					buttonSize = 55;
				} else if(finalDifficulty == 2) {
					buttonSize = 40;
				} else {
					buttonSize = 30;
				}
				buttons[row][col] = new MSButton(buttonValue[row][col], row, col, buttonSize);
				if(finalDifficulty == 3)
					buttons[row][col].setSize(20);
				MSButton b = buttons[row][col];
				
				b.setOnMousePressed(e -> {
					if(!smily.getDead() && !smily.getWin()) {
						smily.setGraphic(smily.imageO);
					} else if(smily.getDead() && !smily.getWin()){
						smily.setGraphic(smily.imageDead);
						timer.stop();
					} else if(smily.getWin() && !smily.getDead()) {
						smily.setGraphic(smily.imageSunGlasses);
						timer.stop();
					}
				});
				b.setOnMouseClicked(e -> {
					MouseButton clicker = e.getButton();
					if(clicker == MouseButton.PRIMARY && !smily.getDead() && !smily.getWin() && !b.getFlagged() && !b.getUncovered()) {
						if(stats.getFirstClick()) {
							setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats, buttons, b);
							stats.setFirstClick(false);
							timer = new Timeline(new KeyFrame(Duration.millis(1000), z ->  {
								seconds++;
								oneSec = seconds % 10;
								tenSec = seconds / 10;
								hunSec = seconds / 100;
								//System.out.println(hunSec + " " + tenSec + " " + oneSec);
								time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + hunSec.intValue() + ".png",20,50,true,true))), 0, 0);
								time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + tenSec.intValue() + ".png",20,50,true,true))), 1, 0);
								time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + oneSec.intValue() + ".png",20,50,true,true))), 2, 0);
							}));
							//System.out.println("Timer created");
							timer.setCycleCount(Timeline.INDEFINITE);
						}
						timerGo();
						open(b, stats, buttons, buttonValue, smily);
						/*
						 */
						if(stats.getUncoveredSquares() == 0) {
							smily.setWin(true);
							smily.setGraphic(smily.imageSunGlasses);
							timer.stop();
						}
					}else if(clicker == MouseButton.PRIMARY && !smily.getDead() && !smily.getWin() && b.getUncovered()) {
						int flagsNeeded = b.value;
						//	System.out.println("Flags: " + flagsNeeded);
						if(isValid(b.getX()-1,b.getY(),stats.getGrid()) && buttons[b.getX()-1][b.getY()].getFlagged()) { //W
							flagsNeeded--;
						}
						if(isValid(b.getX()+1,b.getY(),stats.getGrid()) && buttons[b.getX()+1][b.getY()].getFlagged()) { //E
							flagsNeeded--;
						}
						if(isValid(b.getX()-1,b.getY()-1,stats.getGrid()) && buttons[b.getX()-1][b.getY()-1].getFlagged()) { //NW
							flagsNeeded--;
						}
						if(isValid(b.getX(),b.getY()-1,stats.getGrid()) && buttons[b.getX()][b.getY()-1].getFlagged()) { //S
							flagsNeeded--;
						}
						if(isValid(b.getX(),b.getY()+1,stats.getGrid()) && buttons[b.getX()][b.getY()+1].getFlagged()) { //N
							flagsNeeded--;
						}
						if(isValid(b.getX()+1,b.getY()+1,stats.getGrid()) && buttons[b.getX()+1][b.getY()+1].getFlagged()) { //SE
							flagsNeeded--;
						}
						if(isValid(b.getX()-1,b.getY()+1,stats.getGrid()) && buttons[b.getX()-1][b.getY()+1].getFlagged()) { //SW
							flagsNeeded--;
						}
						if(isValid(b.getX()+1,b.getY()-1,stats.getGrid()) && buttons[b.getX()+1][b.getY()-1].getFlagged()) { //NE
							flagsNeeded--;
						}
						if(flagsNeeded <= 0) {
							if(isValid(b.getX()-1,b.getY(),stats.getGrid()) && !buttons[b.getX()-1][b.getY()].getFlagged()) { //W
								open(buttons[b.getX()-1][b.getY()], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX()+1,b.getY(),stats.getGrid()) && !buttons[b.getX()+1][b.getY()].getFlagged()) { //E
								open(buttons[b.getX()+1][b.getY()], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX()-1,b.getY()-1,stats.getGrid()) && !buttons[b.getX()-1][b.getY()-1].getFlagged()) { //NW
								open(buttons[b.getX()-1][b.getY()-1], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX(),b.getY()-1,stats.getGrid()) && !buttons[b.getX()][b.getY()-1].getFlagged()) { //S
								open(buttons[b.getX()][b.getY()-1], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX(),b.getY()+1,stats.getGrid()) && !buttons[b.getX()][b.getY()+1].getFlagged()) { //N
								open(buttons[b.getX()][b.getY()+1], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX()+1,b.getY()+1,stats.getGrid()) && !buttons[b.getX()+1][b.getY()+1].getFlagged()) { //SE
								open(buttons[b.getX()+1][b.getY()+1], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX()-1,b.getY()+1,stats.getGrid()) && !buttons[b.getX()-1][b.getY()+1].getFlagged()) { //SW
								open(buttons[b.getX()-1][b.getY()+1], stats, buttons, buttonValue, smily);
							}
							if(isValid(b.getX()+1,b.getY()-1,stats.getGrid()) && !buttons[b.getX()+1][b.getY()-1].getFlagged()) { //NE
								open(buttons[b.getX()+1][b.getY()-1], stats, buttons, buttonValue, smily);
							}
						}

					} else if(clicker == MouseButton.SECONDARY && !smily.getDead() && !smily.getWin()) {
						if(!b.getFlagged() && !b.getUncovered()) {
							b.setGraphic(b.imageFlag);
							b.setFlagged(true);
							bombsLeft.minusBomb();
							bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() / 10) + ".png" ,20,50,true,true))), 0, 0);
							bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() % 10) + ".png",20,50,true,true))), 1, 0);
						}else if(b.getFlagged() && !b.getUncovered()) {
							b.setGraphic(b.imageCover);
							b.setFlagged(false);
							bombsLeft.addBomb();
							bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() / 10) + ".png" ,20,50,true,true))), 0, 0);
							bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() % 10) + ".png",20,50,true,true))), 1, 0);
						}
					}

				});
				b.setOnMouseReleased(e -> {
					if(stats.getUncoveredSquares() == 0) {
						smily.setWin(true);
						smily.setGraphic(smily.imageSunGlasses);
					}
					if(!smily.getDead() && !smily.getWin()) {
						smily.setGraphic(smily.imageSmile);
					} else if(smily.getDead()) {
						smily.setGraphic(smily.imageDead);
					} else if(smily.getWin()) {
						smily.setGraphic(smily.imageSunGlasses);
					}

				});
				MSGrid.add(buttons[row][col],  row,  col);
			}
		}

		smily.setOnMouseClicked(e -> {
			stats.setFirstClick(true);
			resetBoard(buttonValue, buttons);
			bombsLeft.setBombs(bangBangs);
			bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() / 10) + ".png" ,20,50,true,true))), 0, 0);
			bombsLeftCounter.add((new ImageView(new Image("file:res/MineSweeper/digits/" + (bombsLeft.getBombs() % 10) + ".png",20,50,true,true))), 1, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 0, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 1, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 2, 0);
			stats.setUncoveredSquares(10);
			timerStop();
			smily.setDead(false);
			smily.setWin(false);
			smily.setGraphic(smily.imageSmile);
		});

		beg.setOnAction(e -> {
			finalDifficulty = 1;
			timerStop();
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 0, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 1, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 2, 0);
			
			restartGame(theStage);
		});
		med.setOnAction(e -> {
			finalDifficulty = 2;
			timerStop();
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 0, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 1, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 2, 0);
			restartGame(theStage);
		});
		high.setOnAction(e -> {
			finalDifficulty = 3;
			timerStop();
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 0, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 1, 0);
			time.add((new ImageView(new Image("file:res/MineSweeper/digits/" + 0 + ".png",20,50,true,true))), 2, 0);
			restartGame(theStage);
		});

		topGrid.add(smily, 1, 0);
		outerPane.setTop(topGrid);
		outerPane.setCenter(MSGrid);
		fullPane.setCenter(outerPane);
		fullPane.setTop(vBox);

		outerPane.setStyle(
				"-fx-background-color: #bfbfbf; -fx-border-color: #787878 #fafafa #fafafa #787878; -fx-border-width:3; -fx-border-radius: 0.001;");
		MSGrid.setStyle(
				"-fx-background-color: #B0C6DA; -fx-border-color: #787878 #fafafa #fafafa #787878; -fx-border-width:3; -fx-border-radius: 0.001;");
		fullPane.setStyle(
				"-fx-background-color: #bfbfbf; -fx-border-color:  #fafafa #787878 #787878 #fafafa; -fx-border-width:3; -fx-border-radius: 0.001;");

		theStage.setScene(new Scene(fullPane));
		theStage.setTitle("MINESWEEPER");

		theStage.show();
		theStage.centerOnScreen();
	}
	public void resetBoard(int[][] buttonValue, MSButton[][] buttons) {
		for(int i = 0; i < buttonValue.length; i++) {
			for(int j = 0; j < buttonValue.length; j++) {
				buttons[i][j].value = 0;
				buttons[i][j].setGraphic(buttons[i][j].imageCover);
				buttonValue[i][j] = 0;
				buttons[i][j].setFlagged(false);
				buttons[i][j].setUncovered(false);
			}
		}
		
	}
	public void setBoard(int bombs, int gridSize, int[][] buttonValue, GameStats stats, MSButton[][] buttons, MSButton b) {

		boolean minesSet = true;
		int minesToPlace = bombs;
		stats.setUncoveredSquares((gridSize * gridSize) - bombs);
		int X;
		int Y;
		int bX = b.getX();
		int bY = b.getY();
		while(minesSet){
			do {
				Y = (int)((Math.random()*gridSize));
				X = (int)((Math.random()*gridSize));
			}while(isValid(bX-1,bY-1,gridSize) && buttons[X][Y] == buttons[bX-1][bY-1] || 
					isValid(bX-1,bY,gridSize) && buttons[X][Y] == buttons[bX-1][bY] || 
					isValid(bX-1,bY+1,gridSize) && buttons[X][Y] == buttons[bX-1][bY+1] ||
					isValid(bX,bY-1,gridSize) && buttons[X][Y] == buttons[bX][bY-1] ||
					isValid(bX,bY,gridSize) && buttons[X][Y] == buttons[bX][bY] || 
					isValid(bX,bY+1,gridSize) && buttons[X][Y] == buttons[bX][bY+1] || 
					isValid(bX+1,bY-1,gridSize) && buttons[X][Y] == buttons[bX+1][bY-1] ||
					isValid(bX+1,bY,gridSize) && buttons[X][Y] == buttons[bX+1][bY] ||
					isValid(bX+1,bY+1,gridSize) && buttons[X][Y] == buttons[bX+1][bY+1] );
			
			if(buttonValue[X][Y] != 10) {
				minesToPlace--;
				buttonValue[X][Y] = 10;
				//			System.out.println("X: " + randomMineX + " Y: " + randomMineY);
			}
			if (minesToPlace <= 0) {
				minesSet = false;
			}

		}
		for(int i = 0; i < buttonValue.length; i++) {
			for(int j = 0; j < buttonValue.length; j++) {

				if(buttonValue[i][j] != 10) {
					if(isValid(i-1,j,gridSize) && buttonValue[i-1][j] == 10) { //W
						buttonValue[i][j]++;
					}
					if(isValid(i+1,j,gridSize) && buttonValue[i+1][j] == 10) { //E
						buttonValue[i][j]++;
					}
					if(isValid(i-1,j-1,gridSize) && buttonValue[i-1][j-1] == 10) { //NW
						buttonValue[i][j]++;
					}
					if(isValid(i,j-1,gridSize) && buttonValue[i][j-1] == 10) { //S
						buttonValue[i][j]++;
					}
					if(isValid(i,j+1,gridSize) && buttonValue[i][j+1] == 10) { //N
						buttonValue[i][j]++;
					}
					if(isValid(i+1,j+1,gridSize) && buttonValue[i+1][j+1] == 10) { //SE
						buttonValue[i][j]++;
					}
					if(isValid(i-1,j+1,gridSize) && buttonValue[i-1][j+1] == 10) { //SW
						buttonValue[i][j]++;
					}
					if(isValid(i+1,j-1,gridSize) && buttonValue[i+1][j-1] == 10) { //NE
						buttonValue[i][j]++;
					}
				}
			}
		}
		for(int row = 0; row < gridSize; row++) {
			for(int col = 0; col < gridSize; col++) {
				buttons[row][col].value = buttonValue[row][col];
			}
		}
	}
	public boolean isValid(int row, int col, int grid) {
		if(row >= 0 && col >= 0 && row < grid && col < grid) {
			return true;
		}
		return false;
	}
	public void restartGame(Stage theStage) {
		start(theStage);
	}
	public void open(MSButton b, GameStats stats, MSButton[][] buttons, int[][] buttonValue, SmilyButton smily) {
		if(!b.getUncovered()) {
			if(b.value == 0) {
				b.setGraphic(b.image0);
				b.setUncovered(true);
				stats.minusSquare();
				if(isValid(b.getX()-1,b.getY(),stats.getGrid())) { //W
					open(buttons[b.getX()-1][b.getY()], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX()+1,b.getY(),stats.getGrid())) { //E
					open(buttons[b.getX()+1][b.getY()], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX()-1,b.getY()-1,stats.getGrid())) { //NW
					open(buttons[b.getX()-1][b.getY()-1], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX(),b.getY()-1,stats.getGrid())) { //S
					open(buttons[b.getX()][b.getY()-1], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX(),b.getY()+1,stats.getGrid())) { //N
					open(buttons[b.getX()][b.getY()+1], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX()+1,b.getY()+1,stats.getGrid())) { //SE
					open(buttons[b.getX()+1][b.getY()+1], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX()-1,b.getY()+1,stats.getGrid())) { //SW
					open(buttons[b.getX()-1][b.getY()+1], stats, buttons, buttonValue, smily);
				}
				if(isValid(b.getX()+1,b.getY()-1,stats.getGrid())) { //NE
					open(buttons[b.getX()+1][b.getY()-1], stats, buttons, buttonValue, smily);
				}

			} else if(b.value == 1) {
				b.setGraphic(b.image1);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 2) {
				b.setGraphic(b.image2);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 3) {
				b.setGraphic(b.image3);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 4) {
				b.setGraphic(b.image4);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 5) {
				b.setGraphic(b.image5);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 6) {
				b.setGraphic(b.image6);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 7) {
				b.setGraphic(b.image7);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 8) {
				b.setGraphic(b.image8);
				b.setUncovered(true);
				stats.minusSquare();
			}else if(b.value == 10) {

				b.setGraphic(b.imageMineRed);

				b.setUncovered(true);
				smily.setGraphic(smily.imageDead);
				smily.setDead(true);
				for(int m = 0; m < buttons.length; m++) {
					for(int n = 0; n < buttons.length; n++) {
						if(buttonValue[m][n] == 10 && buttons[m][n] != b) {
							buttons[m][n].setGraphic(buttons[m][n].imageExplodeMine);
						}
						if(buttonValue[m][n] != 10 && buttons[m][n].getFlagged()) {
							buttons[m][n].setGraphic(buttons[m][n].imageMisflagged);
						}
					}
				}
			}
		}
		if(stats.getUncoveredSquares() == 0) {
			smily.setWin(true);
			smily.setGraphic(smily.imageSunGlasses);
		}
			/*
		}else if (stats.getFirstClick()) {
			
			
			while(b.value > 0) {
				
				System.out.println(b.value);
					if(isValid(b.getX()-1,b.getY(),stats.getGrid()) && buttons[b.getX()-1][b.getY()].value == 10) { //W
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX()+1,b.getY(),stats.getGrid()) && buttons[b.getX()+1][b.getY()].value == 10) { //E
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX()-1,b.getY()-1,stats.getGrid()) && buttons[b.getX()-1][b.getY()-1].value == 10) { //NW
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX(),b.getY()-1,stats.getGrid()) && buttons[b.getX()][b.getY()-1].value == 10) { //S
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX(),b.getY()+1,stats.getGrid()) && buttons[b.getX()][b.getY()+1].value == 10) { //N
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX()+1,b.getY()+1,stats.getGrid()) && buttons[b.getX()+1][b.getY()+1].value == 10) { //SE
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX()-1,b.getY()+1,stats.getGrid()) && buttons[b.getX()-1][b.getY()+1].value == 10) { //SW
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}else if(isValid(b.getX()+1,b.getY()-1,stats.getGrid()) && buttons[b.getX()+1][b.getY()-1].value == 10) { //NE
						resetBoard(buttonValue, buttons);
						setBoard(stats.getBombs(), stats.getGrid(), buttonValue, stats);
					}
			}
			System.out.println("First Clicked makes zero");
			stats.setFirstClick(false);
			open(b, stats, buttons, buttonValue, smily);
		}
		*/
		if(stats.getUncoveredSquares() == 0) {
			smily.setWin(true);
			timerStop();
		}
		//	System.out.println(stats.getUncoveredSquares());
	}
	public void timerGo() {
		
		timer.play();
		//System.out.println("Timer Started");
	}
	public void timerStop() {
		timer.pause();
		seconds = 0;
	}
}

class SmilyButton extends Button implements EventHandler<ActionEvent>{
	boolean dead = false;
	boolean win = false;
	ImageView imageSmile, imageDead, imageSunGlasses, imageO;

	public SmilyButton() {

		double size = 50;
		setMinWidth(size);
		setMaxWidth(size);
		setMinHeight(size);
		setMaxHeight(size);

		imageSmile = new ImageView(new Image("file:res/MineSweeper/face-smile.png"));
		imageDead = new ImageView(new Image("file:res/MineSweeper/face-dead.png"));
		imageSunGlasses = new ImageView(new Image("file:res/MineSweeper/face-win.png"));
		imageO = new ImageView(new Image("file:res/MineSweeper/face-O.png"));

		imageSmile.setFitHeight(size);
		imageSmile.setFitWidth(size);
		imageDead.setFitHeight(size);
		imageDead.setFitWidth(size);
		imageSunGlasses.setFitHeight(size);
		imageSunGlasses.setFitWidth(size);
		imageO.setFitHeight(size);
		imageO.setFitWidth(size);

		setGraphic(imageSmile);
	}
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	public boolean getDead() {
		return dead;
	}
	public void setWin(boolean win) {
		this.win = win;
	}
	public boolean getWin() {
		return win;
	}

	public void handle(ActionEvent e) {

	}
}

class MSButton extends Button implements EventHandler<ActionEvent>{
	int value;
	int x;
	int y;
	double size;
	boolean uncovered = false;
	boolean flag = false;
	ImageView imageCover, image0, image1, image2, image3, image4, image5, image6, image7, image8, imageExplodeMine, imageMineRed, imageFlag, imageMisflagged;

	public MSButton(int value, int x, int y, double size) {
		this.value = value;
		this.x = x;
		this.y = y;
		this.size = size;
		setMinWidth(size);
		setMaxWidth(size);
		setMinHeight(size);
		setMaxHeight(size);

		imageCover = new ImageView(new Image("file:res/MineSweeper/blank.gif"));
		image0 = new ImageView(new Image("file:res/MineSweeper/0.png"));
		image1 = new ImageView(new Image("file:res/MineSweeper/1.png"));
		image2 = new ImageView(new Image("file:res/MineSweeper/2.png"));
		image3 = new ImageView(new Image("file:res/MineSweeper/3.png"));
		image4 = new ImageView(new Image("file:res/MineSweeper/4.png"));
		image5 = new ImageView(new Image("file:res/MineSweeper/5.png"));
		image6 = new ImageView(new Image("file:res/MineSweeper/6.png"));
		image7 = new ImageView(new Image("file:res/MineSweeper/7.png"));
		image8 = new ImageView(new Image("file:res/MineSweeper/8.png"));
		imageExplodeMine = new ImageView(new Image("file:res/MineSweeper/mine-grey.png"));
		imageMineRed = new ImageView(new Image("file:res/MineSweeper/mine-red.png"));
		imageFlag = new ImageView(new Image("file:res/MineSweeper/flag.png"));
		imageMisflagged = new ImageView(new Image("File:res/MineSweeper/mine-misflagged.png"));

		imageCover.setFitHeight(size);
		imageCover.setFitWidth(size);
		image0.setFitHeight(size);
		image0.setFitWidth(size);
		image1.setFitHeight(size);
		image1.setFitWidth(size);
		image2.setFitHeight(size);
		image2.setFitWidth(size);
		image3.setFitHeight(size);
		image3.setFitWidth(size);
		image4.setFitHeight(size);
		image4.setFitWidth(size);
		image5.setFitHeight(size);
		image5.setFitWidth(size);
		image6.setFitHeight(size);
		image6.setFitWidth(size);
		image7.setFitHeight(size);
		image7.setFitWidth(size);
		image8.setFitHeight(size);
		image8.setFitWidth(size);
		imageExplodeMine.setFitHeight(size);
		imageExplodeMine.setFitWidth(size);
		imageFlag.setFitHeight(size);
		imageFlag.setFitWidth(size);
		imageMineRed.setFitHeight(size);
		imageMineRed.setFitWidth(size);
		imageMisflagged.setFitHeight(size);
		imageMisflagged.setFitWidth(size);

		setGraphic(imageCover);
	}
	public void handle(MouseEvent e) {

	}
	public void setUncovered(boolean uncover) {
		uncovered = uncover;
	}
	public boolean getUncovered() {
		return uncovered;
	}
	public void setFlagged(boolean flag) {
		this.flag = flag;
	}
	public boolean getFlagged() {
		return flag;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public void setSize(double size) {
		this.size = size;
	}

	public void handle(ActionEvent e) {

	}

}

class BombsLeftCounter{
	int bombs;
	public BombsLeftCounter(int bombs) {
		this.bombs = bombs;
	}
	public void setBombs(int bombs) {
		this.bombs = bombs;
	}
	public int getBombs() {
		return bombs;
	}
	public void minusBomb() {
		bombs--;
	}
	public void addBomb() {
		bombs++;
	}
}
class GameStats {
	int uncoveredSquares;
	int bombs;
	int grid;
	int time;
	boolean firstClick;
	int diff;
	GameStats(int finalDifficulty){
		if(finalDifficulty == 1) {
			bombs = 20;
			grid = 9;
		} else if(finalDifficulty == 2) {
			bombs = 40;
			grid = 16;
		} else if(finalDifficulty ==3) {
			bombs = 99;
			grid = 24;
		}
		diff = finalDifficulty;
		uncoveredSquares = (grid*grid)-bombs;
		firstClick = true;
		//	System.out.println("grid: " + grid + " bombs: " + bombs + "uncovered sq:" + uncoveredSquares);
	}
	public boolean getFirstClick() {
		return firstClick;
	}
	public void setFirstClick(boolean firstClick) {
		this.firstClick = firstClick;
	}
	public void setUncoveredSquares(int sq) {
		uncoveredSquares = sq;
		//	System.out.println("uncovered has been set to... " + sq);
	}
	public int getUncoveredSquares() {
		return uncoveredSquares;
	}
	public void minusSquare() {
		uncoveredSquares--;
	}
	public void setBombs(int bombs) {
		this.bombs = bombs;
	}
	public int getBombs() {
		return bombs;
	}
	public void setGrid(int grid) {
		this.grid = grid;
	}
	public int getGrid() {
		return grid;
	}
	public int getDiff() {
		return diff;
	}
}
