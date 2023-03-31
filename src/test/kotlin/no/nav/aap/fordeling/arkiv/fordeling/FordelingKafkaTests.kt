package no.nav.aap.fordeling.arkiv.fordeling

/*
@EmbeddedKafka

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [FordelingHendelseKonsument::class])
@TestInstance(PER_CLASS)
*/

public class FordelingKafkaTests {
    /*
    @Autowired
    lateinit var broker : EmbeddedKafkaBroker

    @MockBean
    lateinit var arkiv : ArkivClient

    @MockBean
    lateinit var prodFordeler : AAPFordelerProd

    @MockBean
    lateinit var arena : ArenaClient

    @MockBean
    lateinit var oppgave : OppgaveClient

    // FordelingHendelseKonsument(private val fordeler : FordelingFactory, private val arkiv : ArkivClient, private val enhet : NavEnhetUtvelger,
    // private val beslutter : FordelingBeslutter, private val monkey : ChaosMonkey, private val slack : Slacker) {

    lateinit var fordeler : AAPFordeler
    lateinit var manuellFordeler : AAPManuellFordeler

    @BeforeAll
    fun beforeAll() {
        manuellFordeler = AAPManuellFordeler(oppgave, FordelerConfig.LOCAL)
        fordeler = AAPFordeler(arena, arkiv, ManuellFordelingFactory(listOf(manuellFordeler, AAPManuellFordelerProd(oppgave))), FordelerConfig.LOCAL)
        val f = FordelingFactory(listOf(fordeler, prodFordeler))
        whenever(prodFordeler.cfg).thenReturn(FordelerConfig.PROD_AAP)
    }

    @BeforeEach
    fun beforeEach() {

        reset(arena, arkiv, oppgave)
        whenever(arena.opprettOppgave(TestData.JP, AUTOMATISK_JOURNALFØRING_ENHET)).thenReturn(OPPRETTET)
    }

    @Test
    fun test() {
        val brokerList = broker.brokersAsString;
        println("XXXX  " + arkiv)
    }
   
     */
}