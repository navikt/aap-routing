Name:         skjermede-personer-pip
Namespace:    nom
Labels:       team=nom
Annotations:  deploy.nais.io/client-version: 2023-01-25-b8772a4
              deploy.nais.io/github-actor: RoyGrini
              deploy.nais.io/github-sha: c7fb9c6cbc15e95c9f15e75c8a1ceed2d689668d
              deploy.nais.io/github-workflow-run-url: https://github.com/navikt/skjerming/actions/runs/3995389398
              kubernetes.io/change-cause:
                nais deploy: commit c7fb9c6cbc15e95c9f15e75c8a1ceed2d689668d: https://github.com/navikt/skjerming/actions/runs/3995389398
              nais.io/deploymentCorrelationID: 86c0072e-78d6-4b3e-aef6-374af1f01228
API Version:  nais.io/v1alpha1
Kind:         Application
Metadata:
  Creation Timestamp:  2021-06-23T13:13:40Z
  Finalizers:
    naiserator.nais.io/finalizer
  Generation:  626
  Managed Fields:
    API Version:  nais.io/v1alpha1
    Fields Type:  FieldsV1
    fieldsV1:
      f:metadata:
        f:annotations:
          .:
          f:deploy.nais.io/client-version:
          f:deploy.nais.io/github-actor:
          f:deploy.nais.io/github-sha:
          f:deploy.nais.io/github-workflow-run-url:
          f:kubernetes.io/change-cause:
          f:nais.io/deploymentCorrelationID:
        f:labels:
          .:
          f:team:
      f:spec:
        .:
        f:accessPolicy:
          f:inbound:
            f:rules:
        f:azure:
          f:application:
            f:enabled:
            f:tenant:
        f:env:
        f:envFrom:
        f:image:
        f:ingresses:
        f:kafka:
          .:
          f:pool:
        f:liveness:
          .:
          f:failureThreshold:
          f:initialDelay:
          f:path:
          f:periodSeconds:
        f:port:
        f:prometheus:
          .:
          f:enabled:
          f:path:
        f:readiness:
          .:
          f:failureThreshold:
          f:initialDelay:
          f:path:
          f:periodSeconds:
          f:timeout:
        f:replicas:
          .:
          f:cpuThresholdPercentage:
          f:max:
          f:min:
        f:resources:
          .:
          f:limits:
            .:
            f:cpu:
            f:memory:
          f:requests:
            .:
            f:cpu:
            f:memory:
    Manager:      deployd
    Operation:    Update
    Time:         2021-08-20T14:03:52Z
    API Version:  nais.io/v1alpha1
    Fields Type:  FieldsV1
    fieldsV1:
      f:metadata:
        f:finalizers:
          .:
          v:"naiserator.nais.io/finalizer":
      f:spec:
        f:accessPolicy:
          .:
          f:inbound:
        f:azure:
          .:
          f:application:
      f:status:
        .:
        f:conditions:
        f:correlationID:
        f:deploymentRolloutStatus:
        f:rolloutCompleteTime:
        f:synchronizationHash:
        f:synchronizationState:
        f:synchronizationTime:
    Manager:         naiserator
    Operation:       Update
    Time:            2023-02-01T15:20:36Z
  Resource Version:  2242015182
  UID:               1b05c702-4d1d-40fe-ad04-9f6662597843
Spec:
  Access Policy:
    Inbound:
      Rules:
        Application:  kabal-api
        Cluster:      dev-gcp
        Namespace:    klage
        Application:  sosialhjelp-dialog-api
        Cluster:      dev-gcp
        Namespace:    teamdigisos
        Application:  sosialhjelp-dialog-api-dev
        Cluster:      dev-gcp
        Namespace:    teamdigisos
        Application:  syfoperson
        Cluster:      dev-gcp
        Namespace:    teamsykefravr
        Application:  syfobehandlendeenhet
        Cluster:      dev-gcp
        Namespace:    teamsykefravr
        Application:  sparkel-vilkarsproving
        Cluster:      dev-fss
        Namespace:    tbd
        Application:  okonomiportal
        Cluster:      dev-fss
        Namespace:    okonomi
        Application:  veilarbperson
        Cluster:      dev-fss
        Namespace:    pto
        Application:  arena
        Cluster:      dev-fss
        Namespace:    teamarenanais
        Application:  modiapersonoversikt-api-q0
        Cluster:      dev-fss
        Namespace:    personoversikt
        Application:  modiapersonoversikt-api-q1
        Cluster:      dev-fss
        Namespace:    personoversikt
        Application:  ida
        Cluster:      prod-fss
        Namespace:    traktor
        Application:  nom-skjerming
        Cluster:      dev-gcp
        Namespace:    aap
        Application:  veilarbportefolje
        Cluster:      dev-fss
        Namespace:    pto
        Application:  sosialhjelp-modia-api
        Cluster:      dev-fss
        Namespace:    teamdigisos
        Application:  oh-person-pip
        Cluster:      dev-gcp
        Namespace:    oppgavehandtering
        Application:  arbeidsfordeling-facade
        Cluster:      dev-fss
        Namespace:    oppgavehandtering
        Application:  poao-tilgang
        Cluster:      dev-gcp
        Namespace:    poao
        Application:  yrkesskade-melding-mottak
        Cluster:      dev-gcp
        Namespace:    yrkesskade
        Application:  yrkesskade-saksbehandling-backend
        Cluster:      dev-gcp
        Namespace:    yrkesskade
        Application:  tiltakspenger-skjerming
        Cluster:      dev-gcp
        Namespace:    tpts
        Application:  abac-test
        Cluster:      dev-fss
        Namespace:    teamabac
        Application:  sosialhjelp-modia-api-dev
        Cluster:      dev-gcp
        Namespace:    teamdigisos
        Application:  pensjon-pen-q0
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pen-q1
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pen-q2
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pen-q4
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pen-q5
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-psak-q0
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-psak-q1
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-psak-q2
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-psak-azuread-q2
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-psak-q4
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-psak-q5
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pselv-q0
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pselv-q1
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pselv-q2
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pselv-q4
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  pensjon-pselv-q0
        Cluster:      dev-gcp
        Namespace:    teampensjon
        Application:  pensjon-pselv-q1
        Cluster:      dev-gcp
        Namespace:    teampensjon
        Application:  pensjon-pselv-q2
        Cluster:      dev-gcp
        Namespace:    teampensjon
        Application:  pensjon-pselv-q4
        Cluster:      dev-gcp
        Namespace:    teampensjon
        Application:  jfr-infotrygd-q2
        Cluster:      dev-gcp
        Namespace:    isa
        Application:  jfr-arena-q2
        Cluster:      dev-gcp
        Namespace:    isa
        Application:  gosys-q2
        Cluster:      dev-fss
        Namespace:    isa
        Application:  forstesidegenerator-q2
        Cluster:      dev-gcp
        Namespace:    isa
        Application:  abac-veilarb-q1
        Cluster:      dev-fss
        Namespace:    pto
        Application:  abac-veilarb-q2
        Cluster:      dev-fss
        Namespace:    pto
        Application:  abac-foreldrepenger
        Cluster:      dev-fss
        Namespace:    teamforeldrepenger
        Application:  fpsak
        Cluster:      dev-fss
        Namespace:    teamforeldrepenger
        Application:  fpfordel
        Cluster:      dev-fss
        Namespace:    teamforeldrepenger
        Application:  abac-institusjonsopphold-q0
        Cluster:      dev-fss
        Namespace:    team-rocket
        Application:  abac-institusjonsopphold-q1
        Cluster:      dev-fss
        Namespace:    team-rocket
        Application:  abac-institusjonsopphold-q2
        Cluster:      dev-fss
        Namespace:    team-rocket
        Application:  abac-krr
        Cluster:      dev-fss
        Namespace:    team-rocket
        Application:  abac-tps
        Cluster:      dev-fss
        Namespace:    team-rocket
        Application:  abac-pdl
        Cluster:      dev-fss
        Namespace:    pdl
        Application:  abac-pdl-q0
        Cluster:      dev-fss
        Namespace:    pdl
        Application:  abac-pdl-q1
        Cluster:      dev-fss
        Namespace:    pdl
        Application:  abac-brreg-proxy-q0
        Cluster:      dev-fss
        Namespace:    arbeidsforhold
        Application:  abac-brreg-proxy-q1
        Cluster:      dev-fss
        Namespace:    arbeidsforhold
        Application:  abac-brreg-proxy-q2
        Cluster:      dev-fss
        Namespace:    arbeidsforhold
        Application:  abac-registre-aareg-q0
        Cluster:      dev-fss
        Namespace:    arbeidsforhold
        Application:  abac-registre-aareg-q1
        Cluster:      dev-fss
        Namespace:    arbeidsforhold
        Application:  abac-registre-aareg-q2
        Cluster:      dev-fss
        Namespace:    arbeidsforhold
        Application:  abac-a-inntekt-q0
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-a-inntekt-q1
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-a-inntekt-q2
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-inntektskomponenten-q0
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-inntektskomponenten-q1
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-inntektskomponenten-q2
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-sigrun
        Cluster:      dev-fss
        Namespace:    team-inntekt
        Application:  abac-arena-q0
        Cluster:      dev-fss
        Namespace:    teamarenanais
        Application:  abac-arena-q1
        Cluster:      dev-fss
        Namespace:    teamarenanais
        Application:  abac-arena-q2
        Cluster:      dev-fss
        Namespace:    teamarenanais
        Application:  pensjon-selvbetjening-soknad-alder-backend
        Cluster:      dev-gcp
        Namespace:    pensjonselvbetjening
        Application:  pensjon-selvbetjening-opptjening-backend
        Cluster:      dev-fss
        Namespace:    pensjonselvbetjening
        Application:  pensjon-selvbetjening-opptjening-backend
        Cluster:      dev-gcp
        Namespace:    pensjonselvbetjening
        Application:  abac-saf
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  arena-personhendelse-q2
        Application:  abac-sak-q0
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-sak-q1
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-sak-q2
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-dokumentproduksjon-q0
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-dokumentproduksjon-q1
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-dokumentproduksjon-q2
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-arkiv-v2
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  bisys
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  bidrag-organisasjon-feature
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  bidrag-organisasjon
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  gosys-q1
        Cluster:      dev-fss
        Namespace:    isa
        Application:  abac-sykepenger-q1
        Cluster:      dev-fss
        Namespace:    helsearbeidsgiver
        Application:  abac-pensjon-q0
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  abac-pensjon-q1
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  abac-pensjon-q2
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  abac-pensjon-q4
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  abac-pensjon-q5
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  abac-presys
        Cluster:      dev-fss
        Namespace:    teampensjon
        Application:  oh-personinformasjon
        Cluster:      dev-fss
        Namespace:    oppgavehandtering
        Application:  oh-personinformasjon-q1
        Cluster:      dev-fss
        Namespace:    oppgavehandtering
        Application:  abac-k9
        Cluster:      dev-fss
        Namespace:    k9saksbehandling
        Application:  abac-duplo
        Cluster:      dev-fss
        Namespace:    teamforeldrepenger
        Application:  abac-histark
        Cluster:      dev-fss
        Namespace:    teamdokumenthandtering
        Application:  abac-arbeidsgiver-inkludering
        Cluster:      dev-fss
        Namespace:    toi
        Application:  abac-bidrag-q0
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  abac-bidrag-q1
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  abac-bidrag-q2
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  bidrag-person-feature
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  bidrag-person
        Cluster:      dev-fss
        Namespace:    bidrag
        Application:  pam-cv-api
        Cluster:      dev-fss
        Namespace:    teampam
        Application:  abac-melosys-q1
        Cluster:      dev-fss
        Namespace:    teammelosys
        Application:  abac-melosys-q2
        Cluster:      dev-fss
        Namespace:    teammelosys
  Azure:
    Application:
      Enabled:  true
      Tenant:   trygdeetaten.no
  Env:
    Name:   SPRING_PROFILES_ACTIVE
    Value:  log-logstash,kafka
    Name:   STACK
    Value:  preprod
    Name:   MIN_CACHE_SIZE
    Value:  0
  Env From:
    Secret:  skjermede-personer-pip
  Image:     ghcr.io/navikt/skjerming/skjermede-personer-pip:c7fb9c6cbc15e95c9f15e75c8a1ceed2d689668d
  Ingresses:
    https://skjermede-personer-pip.dev.intern.nav.no
  Kafka:
    Pool:  nav-dev
  Liveness:
    Failure Threshold:  5
    Initial Delay:      60
    Path:               internal/health/liveness
    Period Seconds:     5
  Port:                 8080
  Prometheus:
    Enabled:  true
    Path:     internal/prometheus
  Readiness:
    Failure Threshold:  5
    Initial Delay:      60
    Path:               internal/health/readiness
    Period Seconds:     5
    Timeout:            4
  Replicas:
    Cpu Threshold Percentage:  50
    Max:                       4
    Min:                       2
  Resources:
    Limits:
      Cpu:     1000m
      Memory:  4096Mi
    Requests:
      Cpu:     500m
      Memory:  2048Mi
Status:
  Conditions:
    Last Transition Time:     2023-02-01T15:14:11Z
    Message:                  complete
    Reason:                   RolloutComplete
    Status:                   True
    Type:                     Ready
    Last Transition Time:     2023-02-01T15:14:11Z
    Message:                  complete
    Reason:                   RolloutComplete
    Status:                   False
    Type:                     Stalled
    Last Transition Time:     2023-02-01T15:14:11Z
    Message:                  complete
    Reason:                   RolloutComplete
    Status:                   False
    Type:                     Reconciling
  Correlation ID:             86c0072e-78d6-4b3e-aef6-374af1f01228
  Deployment Rollout Status:  complete
  Rollout Complete Time:      1675264836969068341
  Synchronization Hash:       d19214bed1799f4c
  Synchronization State:      RolloutComplete
  Synchronization Time:       1675264451340809092
Events:                       <none>
