package giteri.run.displaysStuff;

import giteri.meme.entite.CoupleMeme;
import giteri.meme.entite.Entite;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;

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

	// action , attribut, aggrgator factory
	private MemeFactory memeFactory;

	// networkConstructor, memeFactory, WorkerFactory
	private EntiteHandler entiteHandler;

	// Huml, devrait passer par communicationModel
	private Interfaces.DrawerNetworkInterface drawerGraphStream;

	private WriteNRead writeNRead;

	// à voir avec les éléments d'interface
	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
	private static final String ENTER = "ENTER";

	// -- Panel un
	private static final long serialVersionUID = 1L;
	private JButton btPause;
	private JButton btStep;
	private JButton btDisplayDD;
	private JButton btScreenshot;
	private JButton btReset;

	// -- Panel Stat
	JPanel jpPaneTwo;
	private JLabel jlDensityLabel;
	private JLabel jlDensityMaxLabel;
	private JLabel jlDensitySD;
	private double densityMaxValue;
	private double sdDensity;
	private ArrayList<Double> densityValues;
	private double densityValue;
	private double temp = 0.;

	private Hashtable<String, JLabel> times;
	private JLabel time1, time2, time3, time4, time5;
	private JLabel nbActionBySecond;
	private JFormattedTextField probaEvap;
	private JLabel validated;
	private Icon ok;
	private Icon nope;

	// -- Panel simulation
	private JButton launchSimu;
	private JTextField intervalDef;
	private JLabel regexRule;
	private JLabel regexCor;

	// -- Panel lecture de réseau
	private JPanel plNetworkPlaying;
	private JPanel plNetworkRead;
	private JButton btFile;
	private JButton btAnalyze;
	private JButton btFitting;
	private JButton btDisplayInput;
	private JButton btNextStep;
	private JButton bFittingOneStep;
	private JButton btSpecificConfig;
	private JButton btSemiAutomaticStep;
	private JButton btExplo;

	private JTextField tfPath;
	private JLabel jlWorkInProgress;

	private JLabel jlScore;

	// -- Panel génération de réseau
	private JButton btGenerateEmptyNetwork;
	private JButton btGenerateFiftyNetwork;
	private JButton btGenerateFullNetwork;
	private JButton btGenerateScaleFreeNetwork;
	private JButton btGenerateSmallWorldNetwork;
	private JButton btGenerateCompleteNetwork;
	private JButton btPurgeLinks;

	// Les labels qui affichent les informations sur les nodes possédant les
	// memes, le nombre d'activation d'un meme, et les X dernières activation en terme
	// de meme
	private Hashtable<String, JLabel> nodesHavingXoxoMemesLabel;
	private Hashtable<String, JLabel> nbActivationByMemesLabel;
	private Hashtable<String, JLabel> nbLastActivationByMemesLabel;
	private Hashtable<String, Meme> memesTitle; // correspondance nom & meme
	private JLabel lastActionRatioLabel;

	// Meme version pour les couples de memes
	private Hashtable<String, JLabel> nodesHavingCoupleMemesLabel;
	private Hashtable<String, CoupleMeme> couplesTitle;

	// a voir avec les structures de données

	// Correspondance entre un meme et les memes le possédant
	private Hashtable<String, ArrayList<Integer>> nodesHavingXoxoMemes;
	private Hashtable<String, ArrayList<Integer>> nodesHavingCoupleMemes;

	// Nombre de fois ou le meme a été appelé
	private Map<String, Integer> nbActivationByMemes;
	// Nombre de fois ou le meme a été appelé sur les 20 dernieres actions
	private Map<String, Integer> countOfLastMemeActivation;
	// Sur les 100 dernières actions, quel meme a été appelé
	private List<String> lastHundredActionDone;
	private int sizeOfCircularQueue = Configurator.sizeOfCircularForLastActionDone;

	// Séries de donnée pour l'affichage des graphiques
	private XYSeries seriesDegreeDistribution;
	private XYSeries seriesDensity;
	private ArrayList<XYSeries> ArraySeriesMemeAppliances;
	private XYSeriesCollection datasetMemeAppliance;

	// Lorsque c'est la 1er fois qu'une fonction d'écriture est appelée, création des séries.
	private boolean firstAppliance = true;

	// Chart de l'IHM
	private JFreeChart chart;
	private JFreeChart chartDensity;
	private JFreeChart chartMemeAppliance;
	int compteurSerieDensity = 0;

	private ArrayList<Meme> selectedMemeOnSimulation;
	private ArrayList<CoupleMeme> selectedCoupleMemeOnSimulation;

	// à voir avec le reste
	private ModelController modelController;
	private DecimalFormatSymbols otherSymbols;

	private DecimalFormat decimal;
	private NetworkProperties netProp;

	private static int rmv = 1;
	private static int add = 1;
	//</editor-fold>

	public IHM(ModelController modelParam,
			   MemeFactory memeFactory,
			   EntiteHandler entiteHandler,
			   Interfaces.DrawerNetworkInterface drawerGraphStream,
			   WriteNRead wnr) {

		super("-");

		modelController = modelParam;
		this.memeFactory =memeFactory ;

		this.entiteHandler = entiteHandler ;

		this.drawerGraphStream= drawerGraphStream ;
		this.writeNRead = wnr;

		lastHundredActionDone = new ArrayList<>(sizeOfCircularQueue);
		nbActivationByMemes = new Hashtable<String, Integer>();
		countOfLastMemeActivation = new Hashtable<String, Integer>();

		nodesHavingXoxoMemes = new Hashtable<String, ArrayList<Integer>>();
		nodesHavingCoupleMemes = new Hashtable<String, ArrayList<Integer>>();

		nbLastActivationByMemesLabel = new Hashtable<String, JLabel>();
		nbActivationByMemesLabel = new Hashtable<String, JLabel>();
		nodesHavingXoxoMemesLabel = new Hashtable<String, JLabel>();
		nodesHavingCoupleMemesLabel = new Hashtable<String, JLabel>();

		lastActionRatioLabel = new JLabel();
		jlScore = new JLabel();
		densityValues = new ArrayList<>();

		memesTitle = new Hashtable<String, Meme>();
		couplesTitle = new Hashtable<String, CoupleMeme>();
		this.setSelectedMeme(memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING), memeFactory.getCoupleMemes());
		densityMaxValue = 0.0;
		otherSymbols = new DecimalFormatSymbols(Locale.US);
		decimal = new DecimalFormat("",otherSymbols);
		decimal.setMaximumFractionDigits(4); // arrondi à 2 chiffres apres la
		// virgules
		decimal.setMinimumFractionDigits(3);

		nope = UIManager.getIcon("OptionPane.errorIcon");
		ok = UIManager.getIcon("Tree.collapsedIcon");

		times = new Hashtable<String, JLabel>();

		ArraySeriesMemeAppliances = new ArrayList<XYSeries>();
		Init();
	}

	//<editor-fold desc="fonction public, diverses">

	/**
	 * Met en place la liste des memes qui seront utilisés lors de la
	 * simulation.
	 *
	 * @param selectedMeme
	 *            la liste des memes utilisés
	 */
	public void setSelectedMeme(ArrayList<Meme> selectedMeme, ArrayList<CoupleMeme> selectedCouple) {
		this.selectedMemeOnSimulation = selectedMeme;
		this.selectedCoupleMemeOnSimulation = selectedCouple;
		resetHashTableKeys();
	}

	/**
	 * Lorsqu'une entité fait une action, fonction appelée. Mise à jour des
	 * indicateurs
	 *
	 */
	public void handlerActionApply(ActionApplyEvent e) {
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

	@Override
	public void displayInfo(Configurator.ViewMessageType type, List<String> info) {

	}

	/** Affichage des éléments concernants les memes qui sont joués sur la map
	 *
	 * @param nbAction
	 * @param nbActivByMeme
	 * @param nbLastActivByMeme
	 * @param lastXMemeApplied
	 */
	public void displayXLastAction(int nbAction, Map<String, Integer> nbActivByMeme, Map<String,Integer> nbLastActivByMeme, List<String> lastXMemeApplied){
		lastHundredActionDone = lastXMemeApplied;
		nbActivationByMemes = nbActivByMeme;
		countOfLastMemeActivation = nbLastActivByMeme;
		updateInformationDisplay();
	}

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
			jlWorkInProgress.setVisible(!jlWorkInProgress.isVisible());
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
		return this.chartMemeAppliance;
	}

	//</editor-fold>

	//<editor-fold desc="Création des éléments de IHM">

	/**
	 * Initialisation des champs et de la fenetre openGL
	 *
	 */
	private void Init() {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane jTabPane = createMainFrame();
		jTabPane.addTab("Général", createPaneStat());
		jTabPane.addTab("Répartition des complexe-actions (%)", createPaneSimulation());
		jTabPane.addTab("Comparaison de réseaux", createPaneNetworkFitting());
		jTabPane.addTab("Générateur de réseaux", createPaneNetworkGenerator());

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
		btReset = new JButton("Disponible");

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
		jpPaneTwo = new JPanel();
		jpPaneTwo.setLayout(new BoxLayout(jpPaneTwo, BoxLayout.Y_AXIS));

		// Series
		seriesDegreeDistribution = new XYSeries("Nombre de noeuds@degré");
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(seriesDegreeDistribution);

		seriesDensity = new XYSeries("Densité du réseau@time");
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
//		jpPaneTwo.add(createComponentLabelMemeInformation());

		JPanel memeAndDensities = new JPanel();
		memeAndDensities.setLayout(new BoxLayout(memeAndDensities,BoxLayout.X_AXIS));
//		memeAndDensities.add(createComponentLabelMemeInformation());
		memeAndDensities.add(createComponentLabelCoupleInformation());
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

		launchSimu = new JButton("Disponible");
		intervalDef = new JTextField();
		regexRule = new JLabel("0.xx ou [0.Y1]:O.X1;[0.Y2]:0.X2");
		regexCor = new JLabel("Correct");

		paneThree.add(launchSimu);

		datasetMemeAppliance = new XYSeriesCollection();
		chartMemeAppliance = createChartMemeAppliance(datasetMemeAppliance);
		final ChartPanel chartPanelMemeAppliance = new ChartPanel(chartMemeAppliance);
		chartPanelMemeAppliance.setPreferredSize(new java.awt.Dimension(500, 270));
		paneThree.add(chartPanelMemeAppliance);

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
		btDisplayInput = new JButton("display In");
		btNextStep = new JButton("Next Step");
		bFittingOneStep = new JButton("FittingOnce");
		btSpecificConfig = new JButton("Specific Config");
		btSemiAutomaticStep= new JButton("ToggleSemiautoAction");
		btExplo = new JButton("Explo");

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
								.addComponent(btDisplayInput,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btNextStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(bFittingOneStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btSpecificConfig,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btSemiAutomaticStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btExplo,
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
								.addComponent(btDisplayInput,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btNextStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(bFittingOneStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btSpecificConfig,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btSemiAutomaticStep,
										GroupLayout.DEFAULT_SIZE,
										hauteurComponent, hauteurComponent)
								.addComponent(btExplo,
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

//	/**
//	 * Génération du selecteur de meme. Regarde les meme disponibles dans le
//	 * code et les proposes pour l'initialisation dans la simulation.
//	 *
//	 * @return
//	 */
//	private JPanel createComponentMemeSelector() {
//		JPanel allMemeSelector = new JPanel();
//		allMemeSelector.setLayout(new BoxLayout(allMemeSelector,
//				BoxLayout.Y_AXIS));
//
//		for (Meme meme : existingMeme) {
//			JPanel oneLineSelector = new JPanel();
//			JCheckBox cb = new JCheckBox();
//			JLabel memeLabel = new JLabel();
//			JSpinner spiPourcen = new JSpinner();
//
//			oneLineSelector.add(cb);
//			oneLineSelector.add(memeLabel);
//			oneLineSelector.add(spiPourcen);
//			allMemeSelector.add(oneLineSelector);
//		}
//
//		return allMemeSelector;
//	}

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
		}

		// panel.add(lastActionRatioLabel);
		return panel;
	}

	/** génération du panneau contenant les informations sur les nodes.
	 *
	 * @return
	 */
	private JPanel createComponentLabelCoupleInformation() {
		JPanel panel = new JPanel();
		nodesHavingCoupleMemesLabel.clear();
		String nameCombi;

//		nbActivationByMemesLabel.clear();
//		nbLastActivationByMemesLabel.clear();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		for (CoupleMeme couple : selectedCoupleMemeOnSimulation) {

			// Création du label pour la série de noeud possédant tel meme
			couplesTitle.put(couple.getName(), couple);
			JLabel li = new JLabel();
//			JLabel la = new JLabel();
//			JLabel lol = new JLabel();
			JPanel memePane = new JPanel();
			BoxLayout box = new BoxLayout(memePane, BoxLayout.Y_AXIS);
			memePane.setLayout(box);
			nodesHavingCoupleMemesLabel.put(couple.getName(), li);
			// Creation du label pour la série de noeud du nombre d'activation
			// total
//			nbActivationByMemesLabel.put(meme.toString(), la);
			// Creation des labels pour le nombre d'activation sur les 20
			// dernieres fois
//			nbLastActivationByMemesLabel.put(meme.toString(), lol);
			memePane.add(li);
//			memePane.add(la);
//			memePane.add(lol);
			panel.add(memePane);
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
		jlDensitySD = new JLabel();
		jlDensityMaxLabel.setText("DENSITY SD");
		panel.add(jlDensityLabel);
		panel.add(jlDensityMaxLabel);
		panel.add(jlDensitySD);

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

			// c pa propre ca
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
	private JFreeChart createChartMemeAppliance(final XYDataset dataset) {
		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"% de possession des complexe-actions", // chart title
				"temps", // x axis label
				"% de possession", // y axis label
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
				"Evolution de la densité", // chart title
				"Temps de mesure", // x axis label
				"Densité", // y axis label
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
				"Distribution de degré", // chart title
				"Degré", // x axis label
				"#Noeuds", // y axis label
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
		btDisplayDD.addActionListener(e -> {
			netProp = modelController.getCurrentNetProperties(Configurator.activationCodeAllAttrib);
			displayDDChart(netProp.getDd());
		});

		btScreenshot.addActionListener(e -> modelController.takeScreenshot(Optional.ofNullable(null)));

		btPause.addActionListener(new PauseAction());
		btStep.addActionListener(new StepAction());
		btReset.addActionListener(e-> modelController.comparaisonScreenshot());

		btFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("./Model"));
				chooser.setDialogTitle("choosertitle");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					Configurator.defaultPathForReadingNetwork = chooser.getSelectedFile();
					tfPath.setText(""+Configurator.defaultPathForReadingNetwork);

				} else {
					System.out.println("No Selection");
				}
			}
		});

		// TODO [Waypoint]- Appel des fonctions pour la comparaisons des networks
		btAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IReadNetwork fileNetRdr = modelController.getReader();
				NetworkProperties fileReadProperties;
				NetworkProperties currentNetProperties;

				try {
					fileNetRdr = writeNRead.readAndCreateNetwork("" + Configurator.defaultPathForReadingNetwork, fileNetRdr," ", "#");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				fileReadProperties = fileNetRdr.getNetworkProperties();
				currentNetProperties = modelController.getCurrentNetProperties(Configurator.activationCodeAllAttrib);
				// Mettre dnas les bons JP
				plNetworkRead.removeAll();
				plNetworkRead.add(createComponentLabelNetworkAnalyzedInformation(fileReadProperties));

				plNetworkPlaying.removeAll();
				plNetworkPlaying.add(createComponentLabelNetworkAnalyzedInformation(currentNetProperties));

				plNetworkRead.getRootPane().repaint();
				double score = FittingClass.getNetworksDistanceDumb(Configurator.activationCodeForScore, fileReadProperties, currentNetProperties);
				jlScore.setText("Score:" + score );
			}
		});

		btFitting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleEnableInterface();
				modelController.fittingNetworks();
			}
		});

		btDisplayInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { modelController.displayInput();
			}
		});

		btNextStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelController.fittingNextStep();
			}
		});

		bFittingOneStep.addActionListener(e -> modelController.fittingOnce());

		btSpecificConfig.addActionListener(e ->modelController.fittingSpecificConf());

		btSemiAutomaticStep.addActionListener(e -> modelController.toggleActionSemiAuto());

		btExplo.addActionListener(e -> modelController.toggleActionSemiAuto());

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
	private void addXYseriesMemeApplianceWMeme(Set<Meme> memes){
		datasetMemeAppliance.removeAllSeries();
		String labelSerie;
		Color serieColor;
		int serieIndex;

		// Pour chaque série qu'on veut ajouter
		for (Meme meme : memes) {
			labelSerie = memeFactory.translateMemeCombinaisonReadable(meme.toFourCharString());

			// on crée les série dans l'ordre a partir de 0 et regarde le meme associé a cet index dans MemeFactory
			XYSeries aSerie = new XYSeries(labelSerie);
			serieIndex = memeFactory.getIndexFromMeme(meme);
			// label son petit nom
			// Couleur associé a cet index
			serieColor = drawerGraphStream.getColorAsColor(serieIndex);
			chartMemeAppliance.getXYPlot().getRenderer().setSeriesPaint(serieIndex, serieColor);
			// serie.setKey(labelSerie);
			datasetMemeAppliance.addSeries(aSerie);
			ArraySeriesMemeAppliances.add(aSerie);

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

	//<editor-fold desc="mise a jour des informations de l'interface">

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
				boolean refreshDDArray = (compteurSerieDensity % 10 == 0);
				boolean refreshMemeAppliance = refreshDDArray;
				// densité seul
				int activator = 1;
				Color associatedColor;
				int indexCouple ;

				if(refreshDDArray)
					activator = 9;//Configurator.AllAttribActivator;
				// TODO est ce que ca vaut le coup de mettre a jour l'information en direct?
				netProp = modelController.getCurrentNetProperties(activator);

				// On releve le compte des couples sur les entites
				// C'est sale, devrait etre fait avec les event mais trop long
				nodesHavingCoupleMemes.clear();
				for (Entite entite : entiteHandler.getEntitesActive()) {
					// on regarde le couple possédé
					indexCouple = entite.getCoupleMemeIndex();
					// et on met a jour au fur et a mesure la liste de.
					Toolz.addElementInHashArray(nodesHavingCoupleMemes,
							memeFactory.getCoupleMemeFromIndex(indexCouple).getName(),1);
				}

				JLabel lbl;
				int nbNodesWithThisCouple = 0;
				for (CoupleMeme coupleMeme : memeFactory.getCoupleMemes()) {
					// trouve une couleur indexé
					associatedColor = drawerGraphStream.getColorAsColor(coupleMeme.getIndex());
					// trouve le label associé au couple
					lbl = nodesHavingCoupleMemesLabel.get(coupleMeme.getName());
					memeRef = coupleMeme.getName() ;
					String toPut = memeRef + ": [";
					// Si tout les noeuds possede ce meme
					if (nodesHavingCoupleMemes.get(coupleMeme.getName()) != null  && nodesHavingCoupleMemes.get(coupleMeme.getName()).size() == Configurator.getNbNode()){
						toPut += "ALL]";
					}
					// Si tout les noeuds ne sont pas de ce type
					else
					{
						nbNodesWithThisCouple = nodesHavingCoupleMemes.get(coupleMeme.getName()) == null? 0 :
								nodesHavingCoupleMemes.get(coupleMeme.getName()).size();
						toPut += nbNodesWithThisCouple;
						toPut += "]";
					}

					if (lbl != null) {
						lbl.setText(toPut);
						lbl.setForeground(associatedColor);
					}
				}

//				for (Meme meme : selectedMemeOnSimulation) {
//					associatedColor = drawerGraphStream.getColorAsColor(memeFactory.getIndexFromMemeFourChar(meme.toFourCharString()));
//
//					// NOMBRE DE POSSESSION DE MEME PAR LES ENTITES
//					// Savoir quel noeud possède quel meme;
//					JLabel lbl = nodesHavingXoxoMemesLabel.get(meme.toString());
//					// Nom sous forme "add+"
//					memeRef = memeFactory.translateMemeCombinaisonReadable(meme.toString()) ;
//					String toPut = memeRef + ": [";
//					// Si tout les noeuds possede ce meme
//					if (nodesHavingXoxoMemes.get(meme.toString()) != null
//							&& nodesHavingXoxoMemes.get(meme.toString()).size() == Configurator.getNbNode()){
//						toPut += "ALL]";
//					}
//					// Si tout les noeuds ne sont pas de ce type
//					else
//					{
//						int nbNodesWithThisMeme = 0;
//
//						nbNodesWithThisMeme = nodesHavingXoxoMemes.get(meme.toString()) == null? 0 :
//							nodesHavingXoxoMemes.get(meme.toString()).size();
//						toPut += nbNodesWithThisMeme;
//						toPut += "]";
//					}
//
//					if (lbl != null) {
//						lbl.setText(toPut);
//						lbl.setForeground(associatedColor);
//
//						// NOMBRE D'APPEL DES MEMES DEPUIS LE DEBUT DE LA SIMULATION
//						// Savoir combien de fois le meme a été appelé depuis le début de la simulation
//						nbAppel = nbActivationByMemes.get(meme.toString());
//						// LABEL générique
//						nbActivationByMemesLabel.get(meme.toString()).setText(memeRef + ":" + nbAppel );
//						nbActivationByMemesLabel.get(meme.toString()).setForeground(associatedColor);
//						totalAppel += nbAppel;
//						nbAppelInLast100 = countOfLastMemeActivation.containsKey(meme.toString()) ? countOfLastMemeActivation
//								.get(meme.toString()) : 0;
//
//						// Partie last 100 compte du nombre
//						nbLastActivationByMemesLabel.get(meme.toString()).setText(memeRef + ": "
//								+ countOfLastMemeActivation.get(meme.toString()) + "("
//								+ countOfLastMemeActivation.get(meme.toString()) * 100 / sizeOfCircularQueue
//								+"%)");
//						nbLastActivationByMemesLabel.get(meme.toString()).setForeground(associatedColor);
//					}
//				}
//
//				String oldText;
//				// On refait une passe pour mettre a jour les % de possession
//				for (Meme meme : selectedMemeOnSimulation) {
//					if(totalAppel != 0){
//						oldText = nbActivationByMemesLabel.get(meme.toString()).getText();
//						nbActivationByMemesLabel.get(meme.toString()).setText(oldText +
//								"(" + nbActivationByMemes.get(meme.toString()) * 100 / totalAppel +"%)");
//					}
//				}

				densityValue = netProp.getDensity();
				jlDensityLabel.setText("Density: " + Double.parseDouble(decimal.format(densityValue)));

				if (densityValue > densityMaxValue) {
					densityMaxValue = densityValue;
					jlDensityMaxLabel.setText("Density max : " + Double.parseDouble(decimal.format(densityMaxValue)));
				}

				seriesDensity.add(compteurSerieDensity++, netProp.getDensity());
				if (refreshDDArray)
					displayDDChart(netProp.getDd());
				if(Configurator.displayOnIHMDensitySD)
				{
					densityValues.add(netProp.getDensity());
					try {
						temp = Toolz.getNumberCutToPrecision(Toolz.getDeviation(densityValues, Optional.ofNullable(null)), 4);
					}catch (Exception e){}
					jlDensitySD.setText("SD density: " + temp);
				}

			}
			catch(NullPointerException npe){
				if(Configurator.overallDebug)
					System.err.println("[IHM-updateInformationDisplay()]-" + npe.getMessage());
			}
			catch (Exception e){
				if(!Configurator.fullSilent) System.err.println("Erreur de mise a jour de l'interface "+ e.getMessage());
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

	/** Ajoute une timestep au plotting de meme possession. Si c'est une nouvelle configuration, ajoute
	 * d'abord la série a la chart.
	 *
	 * @param time timestamp d'affichage
	 * @param kvIndexValue k: meme, v: pourcentage de possession sur la map. mise à jour dans l'entité handler.OneStep()
	 */
	public void addValueToApplianceSerie(double time, Map<Meme, Double> kvIndexValue){
		if(firstAppliance){
			addXYseriesMemeApplianceWMeme(kvIndexValue.keySet());
			firstAppliance = false;
		}

		String keyMeme;

		// Pour chaque meme, on trouve la traduction en langage lisible, et on cherche la série correspondante.
		for (Meme meme : kvIndexValue.keySet()) {
			keyMeme = memeFactory.translateMemeCombinaisonReadable((meme.toFourCharString()));
			for (XYSeries arraySeriesMemeAppliance : ArraySeriesMemeAppliances) {
				if(keyMeme.compareToIgnoreCase((String)arraySeriesMemeAppliance.getKey()) == 0)
					arraySeriesMemeAppliance.add(time, kvIndexValue.get(meme));
			}
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

	/**
	 * Réinitilise l'interface aux valeurs de base, entre les steps de
	 * simulation par exemple.
	 *
	 */
	public void resetIHM() {

		if(Configurator.isFitting)
			this.setSelectedMeme(memeFactory.getMemes(Configurator.MemeList.FITTING, Configurator.ActionType.ANYTHING),
					memeFactory.getCoupleMemes());
		else
			this.setSelectedMeme(memeFactory.getMemes(Configurator.MemeList.ONMAP, Configurator.ActionType.ANYTHING),
					memeFactory.getCoupleMemes());
		resetHashTableKeys();
		updateInformationDisplay();
		// seriesMemeAppliance.clear();
		for (XYSeries serie : ArraySeriesMemeAppliances) {
			serie.clear();
		}

		firstAppliance = true;

		ArraySeriesMemeAppliances.clear();
		datasetMemeAppliance.removeAllSeries();
	}

	/** Reset la chart de densité over proba, utilisée pour
	 * l'ancienne simu.
	 *
	 */
	public void resetDensityOverProbaChart(){
		seriesDensity.clear();
	}

	//</editor-fold>

	//<editor-fold desc="fonction public, diverses">

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

	//</editor-fold>

	/** Vitesse des tics de la simulation.
	 *
	 *
	 */
	class JSlideListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e){
			JSlider source = (JSlider)e.getSource();
			if (!source.getValueIsAdjusting()) {
				source.setToolTipText("Value "+(int)(source.getValue()));
				int fps = (int)source.getValue();
				Configurator.setThreadSpeed(fps);

			}
		}
	}
}