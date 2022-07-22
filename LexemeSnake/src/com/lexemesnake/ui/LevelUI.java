package com.lexemesnake.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

/**
 * Интерфейс на уровне
 * @author Кравченко Алексей (medhaid)
 */
public class LevelUI extends Page
{
	static public class NewRecordDialog extends Dialog
	{
		public ImageButton continueButton;
		public TextField nameField;
		
		public NewRecordDialog(Skin skin)
		{
			super("", skin, "dialog");
			setTransform(false);
			
			Label titleLabel = new Label("NEW RECORD!!!", skin);
			titleLabel.setAlignment(Align.center);
			
			Label infoLabel = new Label("Enter name:", skin);
			infoLabel.setAlignment(Align.left);
			
			nameField = new TextField("", skin);
			
			continueButton = new ImageButton(skin, "continueButton");
			
			Table contentTable = getContentTable();
			contentTable.add(titleLabel).row();
			contentTable.add(infoLabel).row();
			contentTable.add(nameField);
			
			Table buttonTable = getButtonTable();
			buttonTable.add(continueButton);
		}
	}
	
	static public class WinDialog extends Dialog
	{
		public ImageButton backMenuButton;
		public ImageButton replayTopicButton;
		public ImageButton continueButton;
		public Label infoLabel;
		
		public WinDialog(Skin skin)
		{
			super("", skin, "dialog");
			setTransform(false);
			
			infoLabel = new Label("", skin);
			infoLabel.setAlignment(Align.center);
			backMenuButton = new ImageButton(skin, "backMenuButton");
			replayTopicButton = new ImageButton(skin, "replayTopicButton");
			continueButton = new ImageButton(skin, "continueButton");
			
			Table contentTable = getContentTable();
			contentTable.add(infoLabel);
			
			Table buttonTable = getButtonTable();
			buttonTable.add(backMenuButton);
			buttonTable.add(replayTopicButton);
			buttonTable.add(continueButton);
		}
		
		public void setContent(int scores, String topicName)
		{
			infoLabel.setText("WINNING!!! \nTopic: "+topicName+"\nScores: "+ scores +" ");
		}
	}
	
	static public class LoseDialog extends Dialog
	{
		public ImageButton backMenuButton;
		public ImageButton replayTaskButton;
		public ImageButton continueButton;
		public Label infoLabel;
		
		public LoseDialog(Skin skin)
		{
			super("", skin, "dialog");
			setTransform(false);
			
			infoLabel = new Label("GAME OVER\n You lose, try again", skin);
			infoLabel.setAlignment(Align.center);
			backMenuButton = new ImageButton(skin, "backMenuButton");
			replayTaskButton = new ImageButton(skin, "replayTaskButton");
			continueButton = new ImageButton(skin, "continueButton");
			continueButton.setDisabled(true);
			
			Table contentTable = getContentTable();
			contentTable.add(infoLabel);
			
			Table buttonTable = getButtonTable();
			buttonTable.add(backMenuButton);
			buttonTable.add(replayTaskButton);
			buttonTable.add(continueButton);
		}
	}
	
	public ImageButton backMenuButton;
	public ImageButton soundButton;
	public Label scoresLabel;
	public Label topicLabel;
	public Label taskIndexLabel;
	
	public Label taskLabel;
	public Label answerLabel;
	public Touchpad touchpad;
	
	public WinDialog winDialog;
	public LoseDialog loseDialog;
	public NewRecordDialog newRecordDialog;
	
	public Label flashLabel;
	
	public LevelUI(Skin skin)
	{
		setFillParent(true);
		setTransform(false);
		fill();
		
		//============ Диалоговые окна ====================	
		newRecordDialog = new NewRecordDialog(skin);
		newRecordDialog.setMovable(false);
		Container newRecordDialogContainer = new Container(newRecordDialog);
		newRecordDialogContainer.setFillParent(true);
		
		winDialog = new WinDialog(skin);
		winDialog.setMovable(false);
		Container winDialogContainer = new Container(winDialog);
		winDialogContainer.setFillParent(true);
		
		loseDialog = new LoseDialog(skin);
		loseDialog.setMovable(false);
		Container loseDialogContainer = new Container(loseDialog);
		loseDialogContainer.setFillParent(true);
		
		hideAllDialogs();
		
		//==================================================		
		flashLabel = new Label("", skin, "flash");
		flashLabel.setAlignment(Align.center);
		//==================================================
		
		backMenuButton = new ImageButton(skin, "backMenuButton");
		
		soundButton = new ImageButton(skin, "soundButton");
		soundButton.setChecked(true);
		
		touchpad = new Touchpad(18, skin);
		
		scoresLabel = new Label("scoresLabel", skin, "title");
		scoresLabel.setAlignment(Align.top | Align.center);
		
		topicLabel = new Label("topicLabel", skin, "title");
		topicLabel.setAlignment(Align.top | Align.center);
		
		taskIndexLabel = new Label("taskIndexLabel", skin , "title");
		taskIndexLabel.setAlignment(Align.top | Align.center);
		
		taskLabel = new Label("taskLabel", skin);
		taskLabel.setAlignment(Align.top | Align.left);
		taskLabel.setWrap(true);
		
		answerLabel = new Label("answerLabel", skin, "answer");
		answerLabel.setAlignment(Align.top | Align.left);
		answerLabel.setWrap(true);
		
		VerticalGroup group = new VerticalGroup();
		group.addActor(taskLabel);
		group.addActor(answerLabel);
		group.fill();
		ScrollPane panel = new ScrollPane(group, skin);
		
		//Выравнивание (верха)
		Table upTable = new Table(skin);
		upTable.setBackground("tintedTableUp");
		//upTable.debug();
		upTable.top().pad(8);
		upTable.add(backMenuButton);
		upTable.add(soundButton);
		upTable.add().expandX();
		upTable.add(topicLabel).minWidth(160);
		upTable.add(taskIndexLabel).minWidth(160);
		upTable.add(scoresLabel).minWidth(160);
		
		//Выравнивание
		Table table = new Table();
		//table.debug();
		table.setTransform(false);
		table.setFillParent(true);
		table.bottom().left();
		table.add(upTable).colspan(2).fillX().top();
		table.row();
		table.add(flashLabel).expand().colspan(2).top().padTop(40);
		table.row();
		table.add(panel).pad(8).minSize(300, 160).maxSize(450, 160).fill().left().expandX();
		table.add(touchpad).pad(8).right();
		
		//Добавление на сцену
		WidgetGroup mainGroup = new WidgetGroup();
		mainGroup.setFillParent(true);
		mainGroup.setTransform(false);
		
		mainGroup.addActor(table);
		mainGroup.addActor(winDialogContainer);
		mainGroup.addActor(loseDialogContainer);
		mainGroup.addActor(newRecordDialogContainer);
		
		setWidget(mainGroup);
	}
	
	public void showMessage(String text, Color color)
	{
		showMessage(text, color, -1, null);
	}
	
	public void showMessage(String text, Color color, float delay)
	{
		showMessage(text, color, delay, null);
	}
	
	public void showMessage(String text, Color color, float delay, Runnable run)
	{
		flashLabel.setText(text);
		flashLabel.setColor(color);
		flashLabel.clearActions();
		
		SequenceAction sequence = new SequenceAction();
		sequence.addAction(Actions.alpha(0));
		sequence.addAction(Actions.visible(true));
		sequence.addAction(Actions.alpha(1, 1));
		
		if (delay != -1)
		{
			sequence.addAction(Actions.delay(delay));
			sequence.addAction(Actions.hide());
		
			if (run != null)
				sequence.addAction(Actions.run(run));
		}
		
		flashLabel.addAction(sequence);
	}
	
	public void removeMessage()
	{
		flashLabel.clearActions();
		flashLabel.setText("");
		flashLabel.setVisible(false);
	}
	
	public void hideAllDialogs()
	{
		loseDialog.setVisible(false);
		winDialog.setVisible(false);
		newRecordDialog.setVisible(false);
	}
}
