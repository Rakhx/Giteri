package giteri.run;

import giteri.meme.mecanisme.ActionFactory;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.WorkerFactory;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.ISetValues;
import giteri.run.interfaces.Interfaces.IView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatter;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.network.networkStuff.DrawerGraphStream;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import giteri.tool.other.WriteNRead;
import giteri.fitting.parameters.FittingClass;
import giteri.run.controller.Controller.ModelController;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.IActionApplyListener;
import giteri.meme.event.BehavTransmEvent;
import giteri.meme.event.IBehaviorTransmissionListener;

/**
 * JFrame qui gère l'affichage de l'application.
 *
 */
@SuppressWarnings("unused")
public class IHM extends JFrame implements IActionApplyListener, IBehaviorTransmissionListener, IView {


	//<editor-fold desc="Properties">

	// worker Factory
	NetworkConstructor networkConstructor;

	// action , attribut, aggrgator factory
	MemeFactory memeFactory;

	// StatAndPlot, DrawerInteface ou drawerGraphStream, Network Analyze
	WorkerFactory workerFactory;

	// networkConstructor, memeFactory, WorkerFactory
	EntiteHandler entiteHandler;

	// entiteHandler
	ActionFactory actionFactory ;

	// entitehandler, memefactory, ntworkconstructor
	//DrawerGraphStream drawerGraphStream;
	Interfaces.DrawerInterface drawerGraphStream;


	WriteNRead writeNRead;

	// à voir avec les éléments d'interface
	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
	private static final String ENTER = "ENTER";

	// -- Panel un
	private static final long serialVersionUID = 1L;
	public JButton btPause;
	public JButton btStep;
	public JButton btDisplayDD;
	public JButton btScreenshot;
	public JButton btReset;

	// -- Panel Stat
	JPanel jpPaneTwo;
	public JLabel jlDensityLabel;
	public JLabel jlDensityMaxLabel;
	public double densityMaxValue;
	private double densityValue;

	public Hashtable<String, JLabel> times;
	public JLabel time1, time2, time3, time4, time5;
	public JLabel nbActionBySecond;
	public JFormattedTextField probaEvap;
	public JLabel validated;
	public Icon ok;
	public Icon nope;

	// -- Panel simulation
	public JButton launchSimu;
	public JTextField intervalDef;
	public JLabel regexRule;
	public JLabel regexCor;

	// -- Panel lecture de réseau
	public JPanel plNetworkPlaying;
	public JPanel plNetworkRead;
	public JButton btFile;
	public JButton btAnalyze;
	public JButton btFitting;
	public JButton btPolar;
	public JButton btNextStep;
	public JButton btToggleStep;
	public JButton btRandomConfig;
	public JButton btSemiAutomaticStep;

	public JTextField tfPath;
	public JLabel jlWorkInProgress;

	public JLabel jlScore;

	// -- Panel génération de réseau
	public JButton btGenerateEmptyNetwork;
	public JButton btGenerateFiftyNetwork;
	public JButton btGenerateFullNetwork;
	public JButton btGenerateScaleFreeNetwork;
	public JButton btGenerateSmallWorldNetwork;
	public JButton btGenerateCompleteNetwork;
	public JButton btPurgeLinks;

	// Les labels qui affichent les informations sur les nodes possédant les
	// memes,
	// le nombre d'activation d'un meme, et les X dernières activation en terme
	// de meme
	public Hashtable<String, JLabel> nodesHavingXoxoMemesLabel;
	public Hashtable<String, JLabel> nbActivationByMemesLabel;
	public Hashtable<String, JLabel> nbLastActivationByMemesLabel;
	public Hashtable<String, Meme> memesTitle;
	public JLabel lastActionRatioLabel;

	// a voir avec les structures de données

	// Correspondance entre un meme et les memes le possédant
	public Hashtable<String, ArrayList<Integer>> nodesHavingXoxoMemes;
	// Nombre de fois ou le meme a été appelé
	public Hashtable<String, Integer> nbActivationByMemes;
	// Nombre de fois ou le meme a été appelé sur les 20 dernieres actions
	public Hashtable<String, Integer> countOfLastMemeActivation;
	// Sur les 100 dernières actions, quel meme a été appelé
	public CircularFifoQueue<String> lastHundredActionDone;
	public int sizeOfCircularQueue = 100;

	// Séries de donnée pour l'affichage des graphiques
	XYSeries seriesDegreeDistribution;
	XYSeries seriesDensity;
	XYSeries seriesDensityOverProba;
	ArrayList<XYSeries> seriesAppliances ;
	XYSeriesCollection datasetDensityOverProba ;

	// Chart de l'IHM
	private JFreeChart chart;
	private JFreeChart chartDensity;
	private JFreeChart chartDensityOverProba;
	int compteurSerieDensity = 0;

	// Liste des memes disponibles dans le programme / meme sélectionné pour le
	// run
	private ArrayList<Meme> existingMeme;
	private ArrayList<Meme> selectedMemeOnSimulation;

	// à voir avec le reste
	private ModelController modelController;
	int compteurAction;

	private DecimalFormatSymbols otherSymbols;

	private DecimalFormat decimal;
	private NetworkProperties netProp;

	private static int rmv = 1;
	private static int add = 1;
	//</editor-fold>


	public IHM(ModelController modelParam,
			   NetworkConstructor networkConstructor,
			   MemeFactory memeFactory,
			   WorkerFactory workerFactory,
			   EntiteHandler entiteHandler,
			   ActionFactory actionFactory ,
			   Interfaces.DrawerInterface drawerGraphStream,
			   WriteNRead wnr) {

		super("-");

		modelController = modelParam;
		this.networkConstructor = networkConstructor;
		this.memeFactory =memeFactory ;;
		this.workerFactory= workerFactory ;
		this.entiteHandler =entiteHandler ;
		this.actionFactory =actionFactory ;
		this.drawerGraphStream= drawerGraphStream ;
		this.writeNRead = wnr;

		existingMeme = memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING);
		lastHundredActionDone = new CircularFifoQueue<String>(
				sizeOfCircularQueue);
		nbActivationByMemes = new Hashtable<String, Integer>();
		countOfLastMemeActivation = new Hashtable<String, Integer>();
		nodesHavingXoxoMemes = new Hashtable<String, ArrayList<Integer>>();

		nbLastActivationByMemesLabel = new Hashtable<String, JLabel>();
		nbActivationByMemesLabel = new Hashtable<String, JLabel>();
		nodesHavingXoxoMemesLabel = new Hashtable<String, JLabel>();
		lastActionRatioLabel = new JLabel();
		jlScore = new JLabel();

		memesTitle = new Hashtable<String, Meme>();
		this.setSelectedMeme(existingMeme);
		compteurAction = 0;
		densityMaxValue = 0.0;
		otherSymbols = new DecimalFormatSymbols(Locale.US);
		decimal = new DecimalFormat("",otherSymbols);
		decimal.setMaximumFractionDigits(4); // arrondi à 2 chiffres apres la
		// virgules
		decimal.setMinimumFractionDigits(3);

		nope = UIManager.getIcon("OptionPane.errorIcon");
		ok = UIManager.getIcon("Tree.collapsedIcon");

		times = new Hashtable<String, JLabel>();

		seriesAppliances = new ArrayList<XYSeries>();
		Init();
	}

	//region de fonction public, diverses

	/**
	 * Réinitilise l'interface aux valeurs de base, entre les steps de
	 * simulation par exemple.
	 *
	 */
	public void resetIHM() {
		resetHashTableKeys();
		updateInformationDisplay();
		// seriesDensityOverProba.clear();
		for (XYSeries serie : seriesAppliances) {
			serie.clear();
		}

		seriesAppliances.clear();
		datasetDensityOverProba.removeAllSeries();
	}

	/** Reset la chart de densité over proba, utilisée pour
	 * l'ancienne simu.
	 *
	 */
	public void resetDensityOverProbaChart(){
		seriesDensity.clear();
	}

	/**
	 * Met en place la liste des memes qui seront utilisés lors de la
	 * simulation.
	 *
	 * @param selectedMeme
	 *            la liste des memes utilisés
	 */
	public void setSelectedMeme(ArrayList<Meme> selectedMeme) {
		this.selectedMemeOnSimulation = selectedMeme;
		resetHashTableKeys();
		// createLabelMemeInformation(); WTF
	}

	/**
	 * Lorsqu'une entité fait une action, fonction appelée. Mise à jour des
	 * indicateurs
	 *
	 */
	public void handlerActionApply(ActionApplyEvent e) {

		if(Configurator.displayMemePosessionDuringSimulation){
			// MISE A JOUR DES DONNEES
			if (e.memeApply != null) {
				String elementRemoveOfCircular ="";
				Toolz.addCountToElementInHashArray(nbActivationByMemes, e.memeApply.toString(), 1);

				// partie last twenty
//				countOfLastMemeActivation.clear();
				if(lastHundredActionDone.size() == lastHundredActionDone.maxSize())
				{
					elementRemoveOfCircular = lastHundredActionDone.poll();
					Toolz.removeCountToElementInHashArray(countOfLastMemeActivation, elementRemoveOfCircular, 1);
				}

				lastHundredActionDone.add(e.memeApply.toString());
				Toolz.addCountToElementInHashArray(countOfLastMemeActivation, e.memeApply.toString(), 1);
			}
			// Dans le cas ou il n'y a pas de meme apply, c'est a dire que l'action
			// d'application du meme a échouer.
			else if (Configurator.displayLogRatioLogFailOverFail ||Configurator.displayLogRatioLogFailOverSuccess )
			{
				System.out.println("Aucune action réalisé par l'entité " + e.entite.getIndex() + " :: message " + e.message);
				if (e.message.contains("RMLK"))
					rmv++;
				else if (e.message.contains("ADLK"))
					add++;

				if(Configurator.displayLogRatioLogFailOverFail)
					System.out.println("ratio fail (rmvFail/addFail): " + (double) rmv / add);
				if(Configurator.displayLogRatioLogFailOverSuccess){
					int nbWin = 0;
					for (Integer winTimes : nbActivationByMemes.values())
						nbWin += winTimes;
					System.out.println("Ratio Fail / sucess: " + (double) (rmv + add) / nbWin);
				}
			}

			// Compteur de tour
			if (++compteurAction % Configurator.refreshInfoRate == 0) {
				compteurAction = 0;
				updateInformationDisplay();
			}
		}
	}

	/** On change au niveau des memes possédées par les entités.
	 * Mise a jour des hashTable de données
	 */
	public void handlerBehavTransm(BehavTransmEvent e) {
		if(e.message == Configurator.MemeActivityPossibility.AjoutMeme.toString())
			// Ajout dans la liste des meme possédé par des entités
			Toolz.addElementInHashArray(nodesHavingXoxoMemes, e.meme.toString(), e.entite.getIndex());
		else if((e.message == Configurator.MemeActivityPossibility.RetraitMeme.toString()))
			Toolz.removeElementInHashArray(nodesHavingXoxoMemes, e.meme.toString(), e.entite.getIndex());
		// RETRAIT
	}

	//region INTERFACE IVIEW

	/**
	 * affiche le nombre d'action par seconde.
	 *
	 */
	public void setDisplayNbAction(String message) {
		nbActionBySecond.setText(message + " action/sec");
	}

	/**
	 * Permet d'afficher un message dans le panel de fitting positionné après
	 * les boutons de lancement de fit etc. Si le message est le meme que celui
	 * déja en place, toggle sa visibilité.
	 */
	public void toggleWkProgress(String message) {
		if (jlWorkInProgress.getText().compareTo(message) == 0) {
//			jlWorkInProgress.setVisible(!jlWorkInProgress.isVisible());
		} else {
			jlWorkInProgress.setText(message);
			jlWorkInProgress.setVisible(true);
		}
	}

	public JFreeChart getDDChart() {
		return this.chart;
	}

	public JFreeChart getDensityChart() {
		return this.chartDensity;
	}

	public JFreeChart getDensityOverProbaChart() {
		return this.chartDensityOverProba;
	}

	//endregion

	//endregion

	//<editor-fold desc="Création des éléments de IHM">
	/**
	 * Initialisation des champs et de la fenetre openGL
	 *
	 */
	private void Init() {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane jTabPane = createMainFrame();
		jTabPane.addTab("Stat", createPaneStat());
		// TODO ici pour la pan simulation
		jTabPane.addTab("Simulation", createPaneSimulation());
		jTabPane.addTab("Comparaison", createPaneNetworkFitting());
		jTabPane.addTab("Network Generator", createPaneNetworkGenerator());

		associateKeyBinding(jTabPane);
		associateComportementToButton();
	}

	/**
	 * Création de la fenetre principale, qui va renvoyer un JPane pour
	 * permettre d'y ajouter des composants.
	 *
	 * @return
	 */
	private JTabbedPane createMainFrame() {
		// SLIDER
		JSlider speedOfEvo = new JSlider(JSlider.HORIZONTAL, -1, 30,
				Configurator.getThreadSleepMultiplicateur());
		speedOfEvo.setName("SPEED");

		speedOfEvo.addChangeListener(new JSlideListener());
		speedOfEvo.setPreferredSize(new Dimension(1000, 10));

		GroupLayout layoutPrincipal = new GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layoutPrincipal);
		layoutPrincipal.setAutoCreateGaps(true);
		layoutPrincipal.setAutoCreateContainerGaps(true);

		// TabPanes
		JTabbedPane jTabPane = new JTabbedPane();

		// BUTTON
		btPause = new JButton("PLAY/PAUSE");
		btStep = new JButton("STEP");
		btDisplayDD = new JButton("DisplayDD");
		btScreenshot = new JButton("screenshot");
		btReset = new JButton("RESET");

		// Réunion des éléments dans le layout principale
		int totalWidth = 1300;
		int totalHeight = 800;
		int canvasH = (int) (totalHeight * (8.5 / 10));
		int slideH = (int) ((double) (totalHeight - canvasH) / 2);

		layoutPrincipal.setVerticalGroup(layoutPrincipal
				.createSequentialGroup()
				.addComponent(speedOfEvo, GroupLayout.DEFAULT_SIZE, slideH,
						slideH)
				.addComponent(jTabPane, GroupLayout.DEFAULT_SIZE, canvasH,
						canvasH)
				.addGroup(
						layoutPrincipal
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE, false)
								.addComponent(btPause).addComponent(btStep)
								.addComponent(btDisplayDD)
								.addComponent(btScreenshot)
								.addComponent(btReset)));

		layoutPrincipal.setHorizontalGroup(layoutPrincipal
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(speedOfEvo, GroupLayout.DEFAULT_SIZE, totalWidth,
						totalWidth)
				.addComponent(jTabPane, GroupLayout.DEFAULT_SIZE, totalWidth,
						totalWidth)
				.addGroup(
						layoutPrincipal.createSequentialGroup()
								.addComponent(btPause).addComponent(btStep)
								.addComponent(btDisplayDD)
								.addComponent(btScreenshot)
								.addComponent(btReset)));

		this.setSize(totalWidth, totalHeight);
		return jTabPane;
	}

	/**
	 * Création d'un panel de stat
	 *
	 * @return
	 */
	private JPanel createPaneStat() {
		/* JPanel */jpPaneTwo = new JPanel();
		jpPaneTwo.setLayout(new BoxLayout(jpPaneTwo, BoxLayout.Y_AXIS));

		// Series
		seriesDegreeDistribution = new XYSeries("First");
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(seriesDegreeDistribution);

		seriesDensity = new XYSeries("Second");
		final XYSeriesCollection datasetDensity = new XYSeriesCollection();
		datasetDensity.addSeries(seriesDensity);

		// Gardé pour voir les exemples d'utilisation
//		jpPaneTwo.add(createComponentTextFieldWithRegex(
//				"" + Configurator.getProbaRetraitLien(), true,
//				Optional.ofNullable(null), Optional.of(0.0), Optional.of(1.0),
//				new MerdeImplementor().new evapMerdeImplementator()));
//
//		jpPaneTwo.add(createComponentTextFieldWithRegex(
//				"" + 0, true,
//				Optional.ofNullable(null), Optional.of(1.0),
//				Optional.ofNullable(null),
//				new MerdeImplementor().new elapsedTimeEvapMerdeImplementator()));

		// Element concernant les memes a la disposition des agents
		jpPaneTwo.add(createComponentLabelMemeInformation());

		JPanel memeAndDensities = new JPanel();
		memeAndDensities.setLayout(new BoxLayout(memeAndDensities,BoxLayout.X_AXIS));
		memeAndDensities.add(createComponentLabelMemeInformation());
		memeAndDensities.add(createComponentLabelNetworkInformation());

		// Element concernant les indicateurs de performance
		JPanel ensemble = new JPanel();
		ensemble.setLayout(new BoxLayout(ensemble, BoxLayout.X_AXIS));
		ensemble.add(memeAndDensities);
		ensemble.add(createComponentLabelTimeInformation());

		jpPaneTwo.add(ensemble);

		// Chart
		chart = createChartDegreeDistribution(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		jpPaneTwo.add(chartPanel);

		chartDensity = createChartDensityOverTime(datasetDensity);
		final ChartPanel chartPanelDensity = new ChartPanel(chartDensity);
		chartPanelDensity.setPreferredSize(new java.awt.Dimension(500, 270));
		jpPaneTwo.add(chartPanelDensity);

		return jpPaneTwo;
	}

	/**
	 * Création du panel de review de configuration, permet de save et charger
	 * un fichier de conf.
	 *
	 * @return
	 */
	private JPanel createPaneConfigurationReview(NetworkProperties prop) {
		JPanel conf = new JPanel();
		// conf.setLayout(new BoxLayout(conf, BoxLayout.X_AXIS));
		// conf.add(new JLabel("nbNodes: "+ prop.getNbNodes()+ " | nbEdges: "+
		// prop.getNbEdges()));
		// conf.add(new JLabel("Density: "+ prop.getDensity()));
		return conf;
	}

	/**
	 * Création d'un pane concernant la simulation.
	 *
	 * @return
	 */
	private JPanel createPaneSimulation() {
		JPanel paneThree = new JPanel();

		paneThree.setLayout(new BoxLayout(paneThree, BoxLayout.Y_AXIS));

		launchSimu = new JButton("GoW");
		intervalDef = new JTextField();
		regexRule = new JLabel("0.xx ou [0.Y1]:O.X1;[0.Y2]:0.X2");
		regexCor = new JLabel("Correct");

		paneThree.add(launchSimu);

		seriesDensityOverProba = new XYSeries("Third");
		datasetDensityOverProba = new XYSeriesCollection();
		chartDensityOverProba = createChartDensityOverProba(datasetDensityOverProba);
		final ChartPanel chartPanelDensityOverProba = new ChartPanel(chartDensityOverProba);
		chartPanelDensityOverProba.setPreferredSize(new java.awt.Dimension(500, 270));
		paneThree.add(chartPanelDensityOverProba);

		return paneThree;
	}

	/**
	 * Crée et renvoi un pane qui permet l'ouverture d'un fichier texte de
	 * network, l'analyse des données de ce réseau, la comparaison avec le
	 * réseau en cours.
	 *
	 * @return
	 */
	private JPanel createPaneNetworkFitting() {
		// bouton lancer l'analyse, fenetre de choix du path des files, tableau
		// avec les
		// valeur du réseau en cours et du réseau analysé.

		// VARIABLES
		String fileChoosen = Configurator.defaultPathForReadingNetwork
				.getAbsolutePath();

		// PANEL ET LAYOUT
		JPanel paneNet = new JPanel();
		GroupLayout layout = new GroupLayout(paneNet);
		paneNet.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// COMPOSANTS TODO a quelle point laisser les boutons ici et pas dans le
		// main?
		tfPath = new JTextField(fileChoosen);
		tfPath.setEditable(false);
		tfPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfPath.setText("" + Configurator.defaultPathForReadingNetwork);

			}
		});

		btAnalyze = new JButton("Analyze");
		btFile = new JButton("File...");
		btFitting = new JButton("Fitting");
		btPolar = new JButton("Polar");
		btNextStep = new JButton("Next Step");
		btToggleStep = new JButton("Rien");
		btRandomConfig = new JButton("Find Stability");
		btSemiAutomaticStep= new JButton("ToggleSemiautoAction");

		plNetworkPlaying = new JPanel();
		plNetworkRead = new JPanel();
		jlWorkInProgress = new JLabel("-");

		// PLACEMENT DES COMPOSANTS
		int largeurButton, hauteurComponent, largeurTextField;
		largeurTextField = 300;
		largeurButton = 30;
		hauteurComponent = 5;
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(tfPath, GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btFile, GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btAnalyze,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btFitting,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btPolar,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btNextStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btToggleStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btRandomConfig,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btSemiAutomaticStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(jlWorkInProgress,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(plNetworkPlaying,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE)
								.addComponent(plNetworkRead,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(jlScore,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE))

		);

		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(tfPath, GroupLayout.DEFAULT_SIZE,
										largeurTextField, largeurTextField)
								.addComponent(btFile, GroupLayout.DEFAULT_SIZE,
										largeurButton, largeurButton)
								.addComponent(btAnalyze,
										GroupLayout.DEFAULT_SIZE,
										largeurButton, largeurButton)
								.addComponent(btFitting,
										GroupLayout.DEFAULT_SIZE,
										largeurButton, largeurButton)
								.addComponent(btPolar,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btNextStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btToggleStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btRandomConfig,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btSemiAutomaticStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(jlWorkInProgress,
										GroupLayout.DEFAULT_SIZE,
										largeurButton, largeurButton)

				)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(plNetworkPlaying,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE)
								.addComponent(plNetworkRead,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(jlScore,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE))

		);

		return paneNet;
	}

	/**
	 * Création d'un panel de génération de réseau, propose différent bouton
	 * pour créer des réseaux d'un certains type.
	 *
	 * @return
	 */
	private JPanel createPaneNetworkGenerator() {

		// PANEL ET LAYOUT
		JPanel paneFour = new JPanel();
		GroupLayout layout = new GroupLayout(paneFour);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		paneFour.setLayout(layout);

		// composant
		btGenerateEmptyNetwork = new JButton("Empty");
		btGenerateFiftyNetwork = new JButton("4% density");
		btGenerateFullNetwork = new JButton("30%");
		btGenerateScaleFreeNetwork = new JButton("ScaleFree");
		btGenerateSmallWorldNetwork = new JButton("SmallWorld");
		btGenerateCompleteNetwork = new JButton("Complete");
		btPurgeLinks = new JButton("Purge");

		// Placement des composants
		int largeurButton, hauteurComponent;
		largeurButton = 30;
		hauteurComponent = 5;
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(btGenerateEmptyNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateFiftyNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateFullNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateScaleFreeNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateSmallWorldNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateCompleteNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btPurgeLinks, GroupLayout.DEFAULT_SIZE,
								hauteurComponent, hauteurComponent)));

		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addComponent(btGenerateEmptyNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateFiftyNetwork,
								GroupLayout.DEFAULT_SIZE, largeurButton,
								largeurButton)
						.addComponent(btGenerateFullNetwork,
								GroupLayout.DEFAULT_SIZE, largeurButton,
								largeurButton)
						.addComponent(btGenerateScaleFreeNetwork,
								GroupLayout.DEFAULT_SIZE, largeurButton,
								largeurButton)
						.addComponent(btGenerateSmallWorldNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btGenerateCompleteNetwork,
								GroupLayout.DEFAULT_SIZE, hauteurComponent,
								hauteurComponent)
						.addComponent(btPurgeLinks, GroupLayout.DEFAULT_SIZE,
								hauteurComponent, hauteurComponent)));

		return paneFour;
	}

	/**
	 * Génération du selecteur de meme. Regarde les meme disponibles dans le
	 * code et les proposes pour l'initialisation dans la simulation.
	 *
	 * @return
	 */
	private JPanel createComponentMemeSelector() {
		JPanel allMemeSelector = new JPanel();
		allMemeSelector.setLayout(new BoxLayout(allMemeSelector,
				BoxLayout.Y_AXIS));

		for (Meme meme : existingMeme) {
			JPanel oneLineSelector = new JPanel();
			JCheckBox cb = new JCheckBox();
			JLabel memeLabel = new JLabel();
			JSpinner spiPourcen = new JSpinner();

			oneLineSelector.add(cb);
			oneLineSelector.add(memeLabel);
			oneLineSelector.add(spiPourcen);
			allMemeSelector.add(oneLineSelector);
		}

		return allMemeSelector;
	}

	/** génération du panneau contenant les informations sur les nodes.
	 *
	 * @return
	 */
	private JPanel createComponentLabelMemeInformation() {
		JPanel panel = new JPanel();
		nodesHavingXoxoMemesLabel.clear();
		nbActivationByMemesLabel.clear();
		nbLastActivationByMemesLabel.clear();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		for (Meme meme : selectedMemeOnSimulation) {
			// Création du label pour la série de noeud possédant tel meme
			memesTitle.put(meme.toString(), meme);
			JLabel li = new JLabel();
			JLabel la = new JLabel();
			JLabel lol = new JLabel();
			JPanel memePane = new JPanel();
			BoxLayout box = new BoxLayout(memePane, BoxLayout.Y_AXIS);
			memePane.setLayout(box);
			nodesHavingXoxoMemesLabel.put(meme.toString(), li);
			// Creation du label pour la série de noeud du nombre d'activation
			// total
			nbActivationByMemesLabel.put(meme.toString(), la);
			// Creation des labels pour le nombre d'activation sur les 20
			// dernieres fois
			nbLastActivationByMemesLabel.put(meme.toString(), lol);
			memePane.add(li);
			memePane.add(la);
			memePane.add(lol);

			panel.add(memePane);
			// panel.add(Box.createRigidArea(new Dimension(0,1)));
			// panel.add(createVerticalSeparator());
			// panel.add(Box.createRigidArea(new Dimension(0,1)));
		}

		panel.add(lastActionRatioLabel);
		return panel;
	}

	/**
	 * Génération d'un panneau contenant les informations sur le network.
	 *
	 * @return
	 */
	private JPanel createComponentLabelNetworkInformation() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Element concernant la densité du graphe
		jlDensityLabel = new JLabel();
		jlDensityLabel.setText("DENSITY");
		jlDensityMaxLabel = new JLabel();
		jlDensityMaxLabel.setText("DENSITY MAX");
		panel.add(jlDensityLabel);
		panel.add(jlDensityMaxLabel);

		return panel;
	}

	/**
	 * Génération d'un panneau contenant les informations sur les temps
	 * d'execution .
	 *
	 * @return
	 */
	private JPanel createComponentLabelTimeInformation() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		time1 = new JLabel("one");
		time2 = new JLabel("two long loing");
		time3 = new JLabel();
		time4 = new JLabel();
		time5 = new JLabel("5 5 5 5 5 5");
		nbActionBySecond = new JLabel("0");
		panel.add(time1);
		panel.add(time2);
		panel.add(time3);
		panel.add(time4);
		panel.add(time5);
		panel.add(nbActionBySecond);
		times.put("1", time1);
		times.put("2", time2);
		times.put("3", time3);
		times.put("4", time4);
		times.put("5", time5);

		return panel;
	}

	/**
	 * Génération d'un panel contenant les informations du réseau qui a ete
	 * analysé.
	 *
	 * @return
	 */
	private JPanel createComponentLabelNetworkAnalyzedInformation(
			NetworkProperties np) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		Class<?> c = new NetworkProperties().getClass();
		for (int i = 0; i < c.getFields().length; i++) {
			//region reflection
			JLabel pair;
			String properties;
			Object value = new Object();
			properties = c.getFields()[i].toString().split("\\.")[c.getFields()[i]
					.toString().split("\\.").length - 1];
			Field generic;
			try {
				generic = c.getDeclaredField(properties);
				try {
					value = generic.get(np);
					// System.out.println(properties + " : "+value);
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
			} catch (NoSuchFieldException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			}
			//endregion

			try {
				pair = new JLabel(properties + ": "
						+ Toolz.getNumberCutToPrecision((Double) value, 4));
			} catch (ClassCastException e) {
				pair = new JLabel(properties + ": " + value);
			}

			panel.add(pair);
		}

		// La partie concernant l'affichage de la distribution de degrée
		XYSeries distrib = new XYSeries("Compared" + np.name);
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(distrib);
		int[] distribution = np.getDd();
		for (int i = 0; i < distribution.length; i++) {
			distrib.add(i, distribution[i]);
		}

		final ChartPanel chartDD = new ChartPanel(
				this.createChartDegreeDistribution(dataset));
		chartDD.setPreferredSize(new java.awt.Dimension(500, 270));
		panel.add(chartDD);

		return panel;
	}

	/**
	 * Création d'un formated textfield avec la vérification des valeurs dans
	 * cette dite textbox
	 *
	 * @param initialValue
	 *            la valeur contenue a l'initialisaiton
	 * @param doubleValue
	 *            S'agit t'il d'une value double ou des strings
	 * @param opRegex
	 *            La regex qui va matcher si on est dnas le cas d'une string
	 * @param opMinValue
	 *            la valeur min dans le cas d'un double Double.Min si non
	 *            précisée
	 * @param opMaxValue
	 *            la valeur max dans le cas d'un double Double.max si non
	 *            précisée
	 * @param setter
	 *            Une interface contenant une fonction setValue(Double [...])
	 *            qui sera appelé si la fonction est correcte .
	 * @return Le groupe TextField + label de vérification dans un panel,
	 *         boxlayout en X_AXIS
	 */
	private JPanel createComponentTextFieldWithRegex(String initialValue,
													 boolean doubleValue, Optional<Pattern> opRegex,
													 Optional<Double> opMinValue, Optional<Double> opMaxValue,
													 ISetValues setter) {

		JPanel TextFieldNValidator = new JPanel();
		JLabel validated = new JLabel(ok);
		DefaultFormatter df = new DefaultFormatter();
		JFormattedTextField textField = new JFormattedTextField(df);
		textField.setText("" + initialValue);
		textField.setMaximumSize(new Dimension(100, 20));

		textField.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName() == "value") {
					if (doubleValue) {

						double value = 0;
						boolean succes = false;
						double minValue = Double.MIN_VALUE;
						double maxValue = Double.MAX_VALUE;
						if (opMinValue.isPresent())
							minValue = opMinValue.get();
						if (opMaxValue.isPresent())
							maxValue = opMaxValue.get();

						try {
							value = Double.parseDouble("" + evt.getNewValue());
							if (value >= minValue && value <= maxValue)
								succes = true;
						} catch (NumberFormatException nfe) {
							succes = false;
						}

						if (succes) {
							setter.setValue(value);
							validated.setIcon(ok);
							validated.setSize(10, 10);
						} else {
							validated.setIcon(nope);
						}
					}
				}
			}
		});

		TextFieldNValidator.setLayout(new BoxLayout(TextFieldNValidator,
				BoxLayout.X_AXIS));
		TextFieldNValidator.add(textField);
		TextFieldNValidator.add(validated);

		return TextFieldNValidator;
	}

	/**
	 * Création du graphe pour la distribution de degrée
	 *
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChartDensityOverProba(final XYDataset dataset) {
		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"Meme appliance in volume of direct", // chart title
				"evaporation", // x axis label
				"Density", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
		);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// final StandardLegend legend = (StandardLegend) chart.getLegend();
		// legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
		renderer.setSeriesShapesVisible(1, false);
		plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		// final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// OPTIONAL CUSTOMISATION COMPLETED.
		// Logarithme http://www.jfree.org/forum/viewtopic.php?f=3&t=3724
		return chart;
	}

	/**
	 * Création du graphe pour la distribution de degrée
	 *
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChartDensityOverTime(final XYDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"Density over time", // chart title
				"time", // x axis label
				"Density", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
		);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// final StandardLegend legend = (StandardLegend) chart.getLegend();
		// legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
		renderer.setSeriesShapesVisible(1, false);
		plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		// final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// OPTIONAL CUSTOMISATION COMPLETED.
		// Logarithme http://www.jfree.org/forum/viewtopic.php?f=3&t=3724
		return chart;
	}

	/**
	 * Création du graphe pour la distribution de degrée
	 *
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChartDegreeDistribution(final XYDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"Degree Distribution", // chart title
				"Degree", // x axis label
				"Nb of nodes", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
		);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// final StandardLegend legend = (StandardLegend) chart.getLegend();
		// legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesVisible(1, false);
		plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// OPTIONAL CUSTOMISATION COMPLETED.
		// Logarithme http://www.jfree.org/forum/viewtopic.php?f=3&t=3724
		return chart;
	}

	/**
	 * Associe les comportements aux boutons.
	 *
	 */
	private void associateComportementToButton() {
		btDisplayDD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				netProp = modelController.getCurrentNetProperties(Configurator.activationCodeAllAttribExceptDD);
				displayDDChart(netProp.getDd());
			}
		});

		btScreenshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.takeScreenshot(Optional.ofNullable(null));
			}
		});

		btPause.addActionListener(new PauseAction());

		// new ActionListener() {
		// public void actionPerformed(ActionEvent e)
		// {
		// if(!Configurator.isSystemPaused()){
		// modelController.suspend();
		// }else {
		// modelController.resume();
		// }
		//
		// Configurator.setSystemPaused(!Configurator.isSystemPaused());
		// }
		// });

		// jbNextStep.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e)
		// {
		// modelController.oneStep();
		// }
		// });
		btStep.addActionListener(new StepAction());

		btReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});

//		launchSimu.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				(new Thread() {
//					public void run() {
//					}
//				}).start();
//
//			}
//		});

		btFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("choosertitle");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					Configurator.defaultPathForReadingNetwork = chooser
							.getSelectedFile();

				} else {
					System.out.println("No Selection ");
				}
			}
		});

		btAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IReadNetwork nl = modelController.getReader();
				NetworkProperties readedProperties;
				NetworkProperties cNetworkProperties;

//				try {
//				//	writeNRead.readAndCreateNetwork("" + Configurator.defaultPathForReadingNetwork, nl," ", "#");
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}

//				nl.test();

				readedProperties = nl.getNetworkProperties();
				cNetworkProperties = modelController.getCurrentNetProperties(Configurator.activationCodeAllAttrib);
				// Mettre dnas les bons JP
				plNetworkRead.removeAll();
				plNetworkRead.add(createComponentLabelNetworkAnalyzedInformation(readedProperties));

				plNetworkPlaying.removeAll();
				plNetworkPlaying.add(createComponentLabelNetworkAnalyzedInformation(cNetworkProperties));

				plNetworkRead.getRootPane().repaint();
				double score = FittingClass.getNetworksDistanceDumb(Configurator.activationCodeForScore, readedProperties, cNetworkProperties);
				jlScore.setText("Score:" + score );
			}
		});

		btFitting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
				toggleEnableInterface();
				modelController.fittingNetworks();
			}
		});

		btPolar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.stabilityResearch();
			}
		});

		btNextStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.fittingNextStep();
			}
		});

		btToggleStep.addActionListener(e -> modelController.toggleStep());

		btRandomConfig.addActionListener(e -> modelController.rdmConfig());

		btSemiAutomaticStep.addActionListener(e -> modelController.toggleActionSemiAuto());

		btGenerateEmptyNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.generateEmptyGraph();
			}
		});

		btGenerateFiftyNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.generateFiveGraph();
			}
		});

		btGenerateFullNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.generateTwentyEightGraph();
			}
		});

		btGenerateScaleFreeNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.generateScaleFreeGraph();
			}
		});

		btGenerateSmallWorldNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.generateSmallWorldGraph();
			}
		});

		btGenerateCompleteNetwork.addActionListener(e -> modelController
				.generateFullGraph());

		btPurgeLinks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.purgeLinks();
			}
		});

	}

	/**
	 * Association des actions au keystroke.
	 *
	 * @param focusedElmt
	 */
	private void associateKeyBinding(JTabbedPane focusedElmt) {
		focusedElmt.getInputMap(IFW)
				.put(KeyStroke.getKeyStroke("ENTER"), ENTER);
		focusedElmt.getActionMap().put(ENTER, new StepAction());
	}

	/** Retire et ajouter X serie d'un coup
	 *
	 */
	private void addXYseries(){
		int numbSerie = 0;
		seriesAppliances.add(new XYSeries(seriesAppliances.size()));
		datasetDensityOverProba.removeAllSeries();
		Meme memeConcerne;
		String labelSerie;
		Color serieColor;
		// Pour chaque série qu'on veut ajouter
		for (XYSeries serie : seriesAppliances) {
			// on crée les série dans l'ordre a partir de 0 et regarde le meme associé a cet index dans MemeFactory
			memeConcerne = memeFactory.getMemeFromInteger(numbSerie);
			if(memeConcerne != null){
				// label son petit nom
				labelSerie = entiteHandler.translateMemeCombinaisonReadable(memeConcerne.toFourCharString());
				// Couleur associé a cet index
				serieColor = drawerGraphStream.getColorAsColor(numbSerie);
			}
			else{
				labelSerie = numbSerie+"";
				serieColor = new Color(0, 0, 0);
			}

			serie.setKey(labelSerie);
			datasetDensityOverProba.addSeries(serie);
			chartDensityOverProba.getXYPlot().getRenderer().setSeriesPaint(numbSerie, serieColor);
			numbSerie++;
		}
	}

	/**
	 * toggle les boutons de l'interface
	 *
	 */
	public void toggleEnableInterface() {
		jpPaneTwo.setEnabled(jpPaneTwo.isEnabled());
		btReset.setEnabled(btReset.isEnabled());
	}
	//</editor-fold>

	//region concernant la mise a jour des informations de l'interface

	/** Plot dans la série "Density Over Proba". DEPRECATED.
	 *
	 */
	public void addValueToDensityOverProbaSerie(double x, double y) {
		seriesDensityOverProba.add(x, y);
	}

	/**
	 *
	 * @param time
	 */
	public void addValueToApplianceSerie(double time, Hashtable<Integer, Double> kvIndexValue){
		int diff = kvIndexValue.size() - datasetDensityOverProba.getSeriesCount();
		int cpt = 0;
		for (int i = 0; i < diff; i++) {
			addXYseries();
		}

		for (int indexMeme : kvIndexValue.keySet()) {
			seriesAppliances.get(indexMeme).add(time, kvIndexValue.get(indexMeme));
		}

	}

	/**
	 * Met a jour les entrées des clefs des hashtable en fonction des meme
	 * choisi pour la simulation
	 *
	 */
	private void resetHashTableKeys() {

		lastHundredActionDone.clear();
		nbActivationByMemes.clear();
		countOfLastMemeActivation.clear();
		nodesHavingXoxoMemes.clear();

		for (Meme meme : selectedMemeOnSimulation) {
			nodesHavingXoxoMemes.put(meme.toString(), new ArrayList<Integer>());
			nbActivationByMemes.put(meme.toString(), 0);
			countOfLastMemeActivation.put(meme.toString(), 0);
		}
	}

	/** Mise a jour des données d'affichage concernant les memes et action Fait
	 * le lien entre les données contenues dans les hashtable et l'affichage de
	 * ces dernière dans les labels ou autres.
	 *
	 */
	private void updateInformationDisplay() {
		if (Configurator.displayPlotWhileSimulation)
		{
			try
			{
				int lastAddCount = 0;
				int lastEvapCount = 0;
				int nbAppelInLast100;
				int totalAppel = 0;
				int nbAppel;
				String memeRef = "";
				boolean refreshDDArray = (compteurSerieDensity % 20 == 0);
				// densité seul
				int activator = 1;
				Color associatedColor;

				if(refreshDDArray)
					activator = 9;//Configurator.AllAttribActivator;
				// TODO est ce que ca vaut le coup de mettre a jour l'information en direct?
				netProp = modelController.getCurrentNetProperties(activator);

				for (Meme meme : selectedMemeOnSimulation) {
					associatedColor = drawerGraphStream.getColorAsColor(memeFactory.getColorIndexStringConversion(meme.toFourCharString()));

					// NOMBRE DE POSSESSION DE MEME PAR LES ENTITES
					// Savoir quel noeud possède quel meme;
					JLabel lbl = nodesHavingXoxoMemesLabel.get(meme.toString());
					// Nom sous forme "add+"
					memeRef = entiteHandler.translateMemeCombinaisonReadable(meme.toString()) ;
					String toPut = memeRef + ": [";
					// Si tout les noeuds possede ce meme
					if (nodesHavingXoxoMemes.get(meme.toString()) != null
							&& nodesHavingXoxoMemes.get(meme.toString()).size() == Configurator.nbNode){
						toPut += "ALL]";
					}
					// Si tout les noeuds ne sont pas de ce type
					else
					{
						int nbNodesWithThisMeme = 0;
						
						nbNodesWithThisMeme = nodesHavingXoxoMemes.get(meme.toString()) == null? 0 :
							nodesHavingXoxoMemes.get(meme.toString()).size();
						toPut += nbNodesWithThisMeme;
						toPut += "]";
					}

					if (lbl != null) {
						lbl.setText(toPut);
						lbl.setForeground(associatedColor);

						// NOMBRE D'APPEL DES MEMES DEPUIS LE DEBUT DE LA SIMULATION
						// Savoir combien de fois le meme a été appelé depuis le début de la simulation
						nbAppel = nbActivationByMemes.get(meme.toString());
						// LABEL générique
						nbActivationByMemesLabel.get(meme.toString()).setText(memeRef + ":" + nbAppel );
						nbActivationByMemesLabel.get(meme.toString()).setForeground(associatedColor);
						totalAppel += nbAppel;
						nbAppelInLast100 = countOfLastMemeActivation.contains(meme.toString()) ? countOfLastMemeActivation
								.get(meme.toString()) : 0;

						// Partie last 100 compte du nombre
						nbLastActivationByMemesLabel.get(meme.toString()).setText(memeRef + ": "
								+ countOfLastMemeActivation.get(meme.toString()) + "("
								+ countOfLastMemeActivation.get(meme.toString()) * 100 / Configurator.nbNode
								+"%)");
						nbLastActivationByMemesLabel.get(meme.toString()).setForeground(associatedColor);
					}
				}

				String oldText;
				// On refait une passe pour mettre a jour les % de possession
				for (Meme meme : selectedMemeOnSimulation) {
					if(totalAppel != 0){
						oldText = nbActivationByMemesLabel.get(meme.toString()).getText();
						nbActivationByMemesLabel.get(meme.toString()).setText(oldText +
								"(" + nbActivationByMemes.get(meme.toString()) * 100 / totalAppel +"%)");
					}
				}

				densityValue = netProp.getDensity();
				jlDensityLabel.setText("Density: " + Double.parseDouble(decimal.format(densityValue)));

				if (densityValue > densityMaxValue) {
					densityMaxValue = densityValue;
					jlDensityMaxLabel.setText("Density max : " + Double.parseDouble(decimal.format(densityMaxValue)));
				}

				seriesDensity.add(compteurSerieDensity++, netProp.getDensity());
				if (refreshDDArray) {
					displayDDChart(netProp.getDd());

				}
			}catch (Exception e){
				if(!Configurator.autrucheMode) 	System.err.println("Erreur de mise a jour de l'interface "+ e.getMessage());
			}
		}
	}

	/**
	 * mise a jour des valeurs de la série qui défini le graph de degré de
	 * distribution.
	 *
	 * @param dd
	 */
	private void displayDDChart(int[] dd) {
		// Cas qui peut arriver quand on reset les données du réseau et quo'n
		// demnade dans le meme temps une mise a jour des vlaeurs de l'interfaxe
		if (dd != null) {
			synchronized (seriesDegreeDistribution) {
				seriesDegreeDistribution.clear();
				for (int i = 0; i < dd.length; i++) {
					seriesDegreeDistribution.add(i, dd[i]);
				}
			}
		}
	}

	//endregion

	//region Autres CLASSE
	/** Action de faire avancer step par step.
	 *
	 */
	private class StepAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!Configurator.isSystemPaused())
				modelController.suspend();
			modelController.oneStep();
		}
	}

	/** Action de faire avancer step par step.
	 *
	 */
	public class PauseAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!Configurator.isSystemPaused()) {
				modelController.suspend();
			} else {
				modelController.resume();
			}

			Configurator.setSystemPaused(!Configurator.isSystemPaused());
		}
	}

	static JComponent createVerticalSeparator() {
		JSeparator x = new JSeparator(SwingConstants.VERTICAL);
		x.setPreferredSize(new Dimension(3, 30));
		return x;
	}

	static JComponent createHorizontalSeparator() {
		JSeparator x = new JSeparator(SwingConstants.HORIZONTAL);
		// x.setPreferredSize(new Dimension(50,3));
		return x;
	}

	//endregion

}