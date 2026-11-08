package com.example.data

import com.example.data.model.QuestionType
import com.example.data.model.ThanaweyaQuestion

data class ThanaweyaSubjectItem(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val chapters: List<String>
)

object ThanaweyaDataProvider {

    val subjects = listOf(
        ThanaweyaSubjectItem(
            id = "arabic",
            name = "اللغة العربية",
            iconName = "MenuBook",
            description = "النحو، البلاغة، الأدب، النصوص والقراءة المتحررة",
            chapters = listOf("الوحدة الثانية: المشتقات وإعمالها", "الوحدة الثالثة: المبتدأ والخبر ونواسخه", "الوحدة الرابعة: المنصوبات والمفاعيل", "البلاغة: التجربة الشعرية والمحسنات", "الأدب: المدارس الشعرية والقصة والرواية")
        ),
        ThanaweyaSubjectItem(
            id = "english",
            name = "اللغة الإنجليزية",
            iconName = "Translate",
            description = "القواعد المتقدمة، الكلمات، مهارات القراءة والترجمة والقصة",
            chapters = listOf("Unit 1-2: Past & Perfect Tenses", "Unit 3-4: Future Forms & Modals", "Unit 5-6: Passive Voice & Phrasal Verbs", "Unit 7-8: Conditionals & Wish", "Advanced Vocabulary & Translation Skills")
        ),
        ThanaweyaSubjectItem(
            id = "physics",
            name = "الفيزياء",
            iconName = "Bolt",
            description = "الكهربية التيارية، التأثير المغناطيسي، الحث الكهرومغناطيسي، والفيزياء الحديثة",
            chapters = listOf("الفصل الأول: التيار الكهربي وقانون أوم وكيرشوف", "الفصل الثاني: التأثير المغناطيسي وأجهزة القياس", "الفصل الثالث: الحث الكهرومغناطيسي والدينامو", "الفصل الرابع: دوائر التيار المتردد والرنين", "الفصل الخامس والسادس: الفيزياء الحديثة والليزر")
        ),
        ThanaweyaSubjectItem(
            id = "chemistry",
            name = "الكيمياء",
            iconName = "Science",
            description = "العناصر الانتقالية، التحليل الكيميائي، الاتزان، الكهربية، والكيمياء العضوية",
            chapters = listOf("الباب الأول: العناصر الانتقالية والسلسلة الأولى", "الباب الثاني: التحليل الكيميائي الوصفي والكمي", "الباب الثالث: الاتزان الكيميائي والأيوني", "الباب الرابع: الكيمياء الكهربية وقوانين فاراداي", "الباب الخامس: الكيمياء العضوية والهيدروكربونات")
        ),
        ThanaweyaSubjectItem(
            id = "biology",
            name = "الأحياء",
            iconName = "Biotech",
            description = "الدعامة والحركة، التنسيق الهرموني، التكاثر، المناعة، وDNA & RNA",
            chapters = listOf("الفصل الأول: الدعامة والحركة في النبات والإنسان", "الفصل الثاني: التنسيق الهرموني في الكائنات الحية", "الفصل الثالث: التكاثر في النبات والإنسان", "الفصل الرابع: المناعة في النبات والإنسان", "الفصل الخامس والسادس: الحمض النووي DNA وتخليق البروتين")
        ),
        ThanaweyaSubjectItem(
            id = "geology",
            name = "الجيولوجيا والعلوم البيئية",
            iconName = "Terrain",
            description = "تراكيب الأرض، المعادن، الصخور، الحركات التكتونية، والعلوم البيئية",
            chapters = listOf("الباب الأول: علم الجيولوجيا ومكونات كوكب الأرض", "الباب الثاني: المعادن والأنظمة البلورية", "الباب الثالث: الصخور النارية والرسوبية والمتحولة", "الباب الرابع: الحركات الأرضية والانجراف القاري والزلازل", "الباب الخامس والعلوم البيئية: التوازن الجيولوجي والنظام البيئي")
        )
    )

    val questions: List<ThanaweyaQuestion> = listOf(
        // ========================= 1. الفيزياء (PHYSICS) =========================
        // الاختياري (50%)
        ThanaweyaQuestion(
            id = "phy_mcq_1",
            subjectId = "physics",
            subjectName = "الفيزياء",
            chapterName = "الفصل الأول: التيار الكهربي وقانون أوم وكيرشوف",
            lessonName = "توصيل المقاومات وقانون أوم للدائرة المغلقة",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "متوسط",
            source = "كتاب الامتحان والوزارة",
            questionText = "سلك مقاومته النوعية (ρe) أُعيد تشكيله بحيث زاد طوله بنسبة 20%، فإن النسبة المئوية للزيادة في مقاومته الكهربية تصبح:",
            options = listOf("20%", "40%", "44%", "24%"),
            correctOptionIndex = 2,
            modelAnswer = "عند إعادة تشكيل السلك يظل الحجم ثابتاً: V = A × L، بالتالي A تتناسب عكسياً مع L. المقاومة R تتناسب طردياً مع L². الطول الجديد L₂ = 1.2 L₁، فتصبح R₂ = (1.2)² R₁ = 1.44 R₁. نسبة الزيادة = (1.44 - 1) × 100% = 44%."
        ),
        ThanaweyaQuestion(
            id = "phy_mcq_2",
            subjectId = "physics",
            subjectName = "الفيزياء",
            chapterName = "الفصل الثالث: الحث الكهرومغناطيسي والدينامو",
            lessonName = "المولد الكهربي (الدينامو)",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "صعب",
            source = "كتاب التفوق",
            questionText = "في دينامو تيار متردد، إذا كانت القيمة اللحظية للقوة الدافعة الكهربية بعد زمن (1/300 s) من الوضع العمودي تساوي نصف القيمة العظمى، فإن تردد التيار الناتج يساوي:",
            options = listOf("25 Hz", "50 Hz", "60 Hz", "100 Hz"),
            correctOptionIndex = 0,
            modelAnswer = "تصل emf إلى نصف القيمة العظمى لأول مرة عندما تكون زاوية الدوران θ = 30° من الوضع العمودي. بما أن θ = 360 × f × t، فإن: 30 = 360 × f × (1/300)، ومنها 360f / 300 = 1.2 f = 30، إذن f = 30 / 1.2 = 25 Hz."
        ),
        // المقالي (50%)
        ThanaweyaQuestion(
            id = "phy_essay_1",
            subjectId = "physics",
            subjectName = "الفيزياء",
            chapterName = "الفصل الأول: التيار الكهربي وقانون أوم وكيرشوف",
            lessonName = "قانونا كيرشوف",
            type = QuestionType.ESSAY,
            difficulty = "متوسط",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "علل فيزيائياً مستنداً إلى قوانين حفظ الطاقة والشحنة: لماذا يعتبر قانون كيرشوف الأول تطبيقاً لقانون بقاء الشحنة الكهربية، بينما قانون كيرشوف الثاني تطبيق لقانون بقاء الطاقة؟",
            modelAnswer = "1. قانون كيرشوف الأول (Σ I_in = Σ I_out): لأن الشحنة الكهربية لا تفنى ولا تُستحدث عند أي نقطة تفرع، فكمية الشحنة الداخلة في وحدة الزمن تساوي الخارجة تماماً دون أي تراكم للشحنات.\n2. قانون كيرشوف الثاني (Σ V_B = Σ I.R): ينص على أن مجموع القوة الدافعة الكهربية في مسار مغلق يساوي مجموع فروق الجهد، وهو يعبر عن أن الطاقة الكلية الممنوحة للشحنات من البطاريات تساوي تماماً الطاقة المستهلكة في المقاومات بالمسار المغلق، تحقيقاً لمبدأ بقاء الطاقة.",
            gradingRubric = "توضيح عدم تراكم الشحنات في الأول (درجة واحدة) + توضيح تساوي الطاقة الممنوحة من المصادر مع المستهلكة في المقاومات في الثاني (درجة واحدة)."
        ),
        ThanaweyaQuestion(
            id = "phy_essay_2",
            subjectId = "physics",
            subjectName = "الفيزياء",
            chapterName = "الفصل الثالث: الحث الكهرومغناطيسي والدينامو",
            lessonName = "المحول الكهربي",
            type = QuestionType.ESSAY,
            difficulty = "صعب",
            source = "كتاب الامتحان والوزارة",
            questionText = "محول كهربي مثالي يخفض الجهد من 220V إلى 11V، يتصل ملفه الثانوي بمصباح قدرته 55W. احسب: 1. شدة التيار المار في الملف الابتدائي. 2. فسر سبب عدم استهلاك طاقة في المحول عند فتح دائرة الملف الثانوي رغم توصيل الملف الابتدائي بالمصدر المتردد.",
            modelAnswer = "1. بما أن المحول مثالي، القدرة الابتدائية = الثانوية: P = V_p × I_p = 55 W. إذن I_p = 55 / 220 = 0.25 A.\n2. عند فتح دائرة الثانوي، يتولد في الملف الابتدائي بالحث الذاتي قوة دافعة كهربية مستحثة عكسية تكاد تساوي تقريباً جهد المصدر وتعاكسه في الاتجاه، فينعدم التيار تقريباً ولا تُستهلك أي طاقة كهربية.",
            gradingRubric = "حساب شدة التيار الابتدائي 0.25A بدقة (درجة) + التعليل الفيزيائي بالحث الذاتي و emf العكسية المتولدة في الابتدائي (درجة)."
        ),

        // ========================= 2. الكيمياء (CHEMISTRY) =========================
        // الاختياري (50%)
        ThanaweyaQuestion(
            id = "chem_mcq_1",
            subjectId = "chemistry",
            subjectName = "الكيمياء",
            chapterName = "الباب الأول: العناصر الانتقالية والسلسلة الأولى",
            lessonName = "الخواص العامة لعناصر السلسلة الانتقالية الأولى",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "متوسط",
            source = "كتاب الامتحان والوزارة",
            questionText = "أي من الأيونات الآتية يمتلك أكبر عزم مغناطيسي في الحالة المستقرة؟",
            options = listOf("Fe³⁺ (Z=26)", "Cr³⁺ (Z=24)", "Mn⁴⁺ (Z=25)", "Cu²⁺ (Z=29)"),
            correctOptionIndex = 0,
            modelAnswer = "التوزيع الإلكتروني لـ Fe³⁺ هو [Ar] 3d⁵، ويحتوي على 5 إلكترونات مفردة في المستوى الفرعي d (وهو الحد الأقصى للإلكترونات المفردة)، وبالتالي يمتلك أكبر عزم مغناطيسي (حوالي 5.92 BM)."
        ),
        ThanaweyaQuestion(
            id = "chem_mcq_2",
            subjectId = "chemistry",
            subjectName = "الكيمياء",
            chapterName = "الباب الثالث: الاتزان الكيميائي والأيوني",
            lessonName = "قاعدة لوشاتيليه وحاصل الإذابة",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "صعب",
            source = "كتاب الأضواء والتفوق",
            questionText = "في التفاعل المتزن الطارد للحرارة: N₂(g) + 3H₂(g) ⇌ 2NH₃(g)، لزيادة كمية غاز النشادر الناتجة يلزم:",
            options = listOf("رفع درجة الحرارة وخفض الضغط", "خفض درجة الحرارة وزيادة الضغط", "إضافة عامل حفاز وخفض الضغط", "رفع درجة الحرارة وزيادة الحجم"),
            correctOptionIndex = 1,
            modelAnswer = "طبقاً لقاعدة لوشاتيليه: 1) التفاعل طارد للحرارة (ΔH سالبة)، لذا خفض درجة الحرارة يزيح الاتزان في الاتجاه الطردي. 2) عدد مولات المتفاعلات (4) أكبر من النواتج (2)، فزيادة الضغط تزيح الاتزان نحو الحجم الأقل (تكوين النشادر)."
        ),
        // المقالي (50%)
        ThanaweyaQuestion(
            id = "chem_essay_1",
            subjectId = "chemistry",
            subjectName = "الكيمياء",
            chapterName = "الباب الثاني: التحليل الكيميائي الوصفي والكمي",
            lessonName = "الكشف عن الأنيونات والكاتيونات",
            type = QuestionType.ESSAY,
            difficulty = "متوسط",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "كيف تميز عملياً بدون استخدام كواشف كيميائية إضافية بين ملح كبريتيت الصوديوم الصلب وملح كبريتيد الصوديوم الصلب باستخدام حمض الهيدروكلوريك المخفف (HCl)؟ اذكر المشاهدة ومعادلات التفاعل بالكلمات أو الرموز.",
            modelAnswer = "بإضافة حمض الهيدروكلوريك المخفف (HCl) إلى كل منهما على حدة:\n1. مع كبريتيت الصوديوم (Na₂SO₃): يتصاعد غاز ثاني أكسيد الكبريت (SO₂) ذو رائحة نفاذة، يخضر ورقة مبللة بمحلول ثاني كرومات البوتاسيوم المحمضة.\n2. مع كبريتيد الصوديوم (Na₂S): يتصاعد غاز كبريتيد الهيدروجين (H₂S) ذو رائحة كريهة (رائحة البيض الفاسد)، ويسود ورقة مبللة بمحلول أسيتات الرصاص الثنائي.",
            gradingRubric = "ذكر الغاز المتصاعد والرائحة النفاذة مع الكبريتيت (درجة) + ذكر غاز H2S ورائحته الكريهة وتسويد ورقة الأسيتات مع الكبريتيد (درجة)."
        ),
        ThanaweyaQuestion(
            id = "chem_essay_2",
            subjectId = "chemistry",
            subjectName = "الكيمياء",
            chapterName = "الباب الخامس: الكيمياء العضوية والهيدروكربونات",
            lessonName = "الكحولات والألكينات وميكانيكية التفاعل",
            type = QuestionType.ESSAY,
            difficulty = "صعب",
            source = "كتاب الامتحان والوزارة",
            questionText = "وضح بالمعادلات الكيميائية المتزنة والشروط كيف يمكنك تحويل الإيثانول إلى إيثيلين جليكول، مع ذكر أسماء التفاعلات المستخدمة.",
            modelAnswer = "الخطوة الأولى: نزع الماء من الإيثانول بالتسخين مع حمض الكبريتيك المركز عند 180°C للحصول على الإيثين (C₂H₄):\nC₂H₅OH --(Conc. H₂SO₄ / 180°C)--> C₂H₄ + H₂O (تفاعل نزع ماء).\n\nالخطوة الثانية: أكسدة الإيثين بواسطة محلول برمنجانات البوتاسيوم في وسط قلوي (تفاعل باير) لإضافة مجموعتي هيدروكسيل وتكوين الإيثيلين جليكول:\nCH₂=CH₂ + H₂O + [O] --(KMnO₄ / قلوي)--> CH₂OH-CH₂OH (تفاعل أكسدة وإضافة).",
            gradingRubric = "كتابة خطوة نزع الماء مع الشروط 180 درجة مئوية (درجة) + كتابة تفاعل باير بالأكسدة في وسط قلوي (درجة)."
        ),

        // ========================= 3. الأحياء (BIOLOGY) =========================
        // الاختياري (50%)
        ThanaweyaQuestion(
            id = "bio_mcq_1",
            subjectId = "biology",
            subjectName = "الأحياء",
            chapterName = "الفصل الأول: الدعامة والحركة في النبات والإنسان",
            lessonName = "الانقباض العضلي وآلية الانزلاق",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "متوسط",
            source = "كتاب الامتحان والوزارة",
            questionText = "أي الأيونات التالية تلعب الدور الأساسي في نقل السيال العصبي من النهاية العصبية إلى غشاء الليفة العضلية عبر شق التشابك؟",
            options = listOf("أيونات الصوديوم (Na⁺)", "أيونات الكالسيوم (Ca²⁺)", "أيونات البوتاسيوم (K⁺)", "أيونات الكلور (Cl⁻)"),
            correctOptionIndex = 1,
            modelAnswer = "عند وصول السيال العصبي إلى الانتفاخات العصبية، تنفتح مضخات الكالسيوم فتدخل أيونات الكالسيوم (Ca²⁺) وتفجر حويصلات التشابك العصبية لتحرير الناقل الكيميائي (الأسيتيل كولين) الذي يعبر شق التشابك."
        ),
        ThanaweyaQuestion(
            id = "bio_mcq_2",
            subjectId = "biology",
            subjectName = "الأحياء",
            chapterName = "الفصل الخامس والسادس: الحمض النووي DNA وتخليق البروتين",
            lessonName = "تضاعف DNA وشفرات الأحماض الأمينية",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "صعب",
            source = "كتاب التفوق والوزارة",
            questionText = "إذا كان عدد النيوكليوتيدات في قطعة من جزيء DNA المزدوج يساوي 600 نيوكليوتيدة، فإن أقصى عدد من الأحماض الأمينية في سلسلة عديد الببتيد التي تشفرها هذه القطعة يساوي:",
            options = listOf("100 حمض أميني", "99 حمض أميني", "199 حمض أميني", "299 حمض أميني"),
            correctOptionIndex = 1,
            modelAnswer = "قطعة DNA مزدوجة بها 600 نيوكليوتيدة، إذن شريط mRNA المنسوخ منها يحتوي على 300 نيوكليوتيدة (شريط مفرد). كل كودون يتكون من 3 نيوكليوتيدات: 300 / 3 = 100 كودون. بما أن آخر كودون هو كودون وقف (Stop Codon) لا يترجم إلى حمض أميني، فإن عدد الأحماض الأمينية = 100 - 1 = 99 حمضاً أمينياً."
        ),
        // المقالي (50%)
        ThanaweyaQuestion(
            id = "bio_essay_1",
            subjectId = "biology",
            subjectName = "الأحياء",
            chapterName = "الفصل الرابع: المناعة في النبات والإنسان",
            lessonName = "المناعة الخلوية والخلايا التائية",
            type = QuestionType.ESSAY,
            difficulty = "متوسط",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "فسر بيولوجياً: كيف تؤثر الخلايا التائية المساعدة (TH) في تنشيط كل من المناعة الخلطية (بالأجسام المضادة) والمناعة الخلوية (بالخلايا التائية) عند دخول ميكروب للجسم؟",
            modelAnswer = "عندما ترتبط الخلية التائية المساعدة (TH) بمركب مولد الضد وبروتين التوافق النسيجي (MHC) على سطح الخلايا البلعمية الكبيرة:\n1. تنشط وتفرز بروتينات (الإنترلوكين) التي تنشط الخلايا البائية للانقسام والتمايز إلى خلايا بائية بلازمية تنتج الأجسام المضادة (مناعة خلطية).\n2. تفرز أيضاً بروتينات (السيتوكينات) التي تجذب الخلايا البلعمية وتنشط الخلايا القاتلة الطبيعية (NK) والخلايا التائية السامة (TC) والخلايا التائية المساعدة الأخرى، مما يشعل المناعة الخلوية لمهاجمة الخلايا المصابة.",
            gradingRubric = "توضيح دور الإنترلوكينات في تنشيط الخلايا البائية للمناعة الخلطية (درجة) + توضيح دور السيتوكينات في تنشيط TC و NK للمناعة الخلوية (درجة)."
        ),
        ThanaweyaQuestion(
            id = "bio_essay_2",
            subjectId = "biology",
            subjectName = "الأحياء",
            chapterName = "الفصل الثاني: التنسيق الهرموني في الكائنات الحية",
            lessonName = "الغدة النخامية والغدة الدرقية وهرمون الكالسيتونين",
            type = QuestionType.ESSAY,
            difficulty = "صعب",
            source = "كتاب الامتحان والوزارة",
            questionText = "ماذا يحدث عند حدوث هبوط مفاجئ في مستوى الكالسيوم في دم شخص بالغ؟ اذكر اسم الهرمون المفرز، الغدة المفرزة له، وآلية عمله لإعادة التوازن الطبيعي.",
            modelAnswer = "عند انخفاض مستوى الكالسيوم في الدم:\n1. تنشط الغدد جارات الدرقية لإفراز هرمون الباراثورمون (PTH).\n2. يعمل الباراثورمون على رفع نسبة الكالسيوم في الدم حتى تصل للمستوى الطبيعي من خلال: تحفيز سحب الكالسيوم من العظام، وزيادة إعادة امتصاص الكالسيوم في الأنابيب الكلوية، وتحفيز امتصاص الكالسيوم من الأمعاء الدقيقة بمساعدة فيتامين د.",
            gradingRubric = "تحديد هرمون الباراثورمون والغدد جارات الدرقية (درجة) + شرح آليات عمله بسحب الكالسيوم من العظام وزيادة امتصاصه كلوياً وهضمياً (درجة)."
        ),

        // ========================= 4. الجيولوجيا والعلوم البيئية (GEOLOGY) =========================
        // الاختياري (50%)
        ThanaweyaQuestion(
            id = "geo_mcq_1",
            subjectId = "geology",
            subjectName = "الجيولوجيا والعلوم البيئية",
            chapterName = "الباب الأول: علم الجيولوجيا ومكونات كوكب الأرض",
            lessonName = "التراكيب الجيولوجية الأولية والثانوية (الفوالق والطيات)",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "متوسط",
            source = "كتاب الامتحان والوزارة",
            questionText = "طية تتكون من 4 طبقات صخرية، فإن النسبة بين (عدد المحاور : عدد الأجنحة : عدد المستويات المحورية) على الترتيب هي:",
            options = listOf("4 : 2 : 1", "2 : 4 : 1", "1 : 2 : 4", "4 : 1 : 2"),
            correctOptionIndex = 0,
            modelAnswer = "لأي طية: مستوى محوري واحد فقط (1)، وجناحان اثنان دائماً (2)، وعدد المحاور يساوي عدد الطبقات (4). إذن النسبة (محاور : أجنحة : مستويات محورية) = 4 : 2 : 1."
        ),
        ThanaweyaQuestion(
            id = "geo_mcq_2",
            subjectId = "geology",
            subjectName = "الجيولوجيا والعلوم البيئية",
            chapterName = "الباب الرابع: الحركات الأرضية والانجراف القاري والزلازل",
            lessonName = "الموجات الزلزالية وحركة الألواح",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "صعب",
            source = "كتاب التفوق والوزارة",
            questionText = "أي المحطات الزلزالية التالية لا تسجل أي نوع من الموجات الزلزالية (الأولية أو الثانوية) الناتجة عن زلزال معين؟",
            options = listOf("المحطات الواقعة بين زاوية 0° إلى 105°", "المحطات الواقعة بين زاوية 105° إلى 140°", "المحطات الواقعة بين زاوية 140° إلى 180°", "المحطات الواقعة عند مركز الزلزال السطحي"),
            correctOptionIndex = 1,
            modelAnswer = "المنطقة من زاوية 105° إلى 140° من بؤرة الزلزال تُعرف بمنطقة الظل الزلزالي (Shadow Zone)؛ حيث تنكسر الموجات الأولية وتتشتت عند انتقالها للب اللب، بينما تتوقف الموجات الثانوية تماماً عند اللب الخارجي المنصهر، فلا ترصد المحطة أياً منهما."
        ),
        // المقالي (50%)
        ThanaweyaQuestion(
            id = "geo_essay_1",
            subjectId = "geology",
            subjectName = "الجيولوجيا والعلوم البيئية",
            chapterName = "الباب الثالث: الصخور النارية والرسوبية والمتحولة",
            lessonName = "دورة الصخور وصخور الأنديزيت والبازلت",
            type = QuestionType.ESSAY,
            difficulty = "متوسط",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "علل جيولوجياً: يختلف صخر الجرانيت عن صخر الأوبسيديان في النسيج الصخري ووضوح البلورات رغم أنهما ينتميان لنفس التركيب الكيميائي والمعدني الحامضي.",
            modelAnswer = "يرجع الاختلاف إلى مكان التبلور وسرعة التبريد للصهير:\n1. الجرانيت: صخر ناري جوفي تبلور في باطن الأرض بتبريد بطيء جداً، مما أتاح وقتاً كافياً لتجمع الأيونات على مراكز التبلور فكون بلورات كبيرة الحجم ترى بالعين المجردة (نسيج خشن الحبيبات).\n2. الأوبسيديان: صخر ناري سطحي (بركاني) برد سريعاً جداً على سطح الأرض عند ملامسته للهواء، فلم تتاح فرصة لتبلور الأيونات فكون نسيجاً عديم التبلور (زجاجياً).",
            gradingRubric = "توضيح تبريد الجرانيت الجوفي البطيء والنسيج الخشن ذي البلورات المرئية (درجة) + توضيح تبريد الأوبسيديان السريع السطحي والنسيج الزجاجي عديم البلورات (درجة)."
        ),
        ThanaweyaQuestion(
            id = "geo_essay_2",
            subjectId = "geology",
            subjectName = "الجيولوجيا والعلوم البيئية",
            chapterName = "الباب الثاني: المعادن والأنظمة البلورية",
            lessonName = "الخواص التماسكية والصلادة ومقياس موهس",
            type = QuestionType.ESSAY,
            difficulty = "صعب",
            source = "كتاب الامتحان والوزارة",
            questionText = "إذا كان لديك عينة معدنية مجهولة تخدش الكالسيت والفلوريت ولكنها تُخدش بالمسامير الفولاذية، فما المدى التقديري لصلادتها؟ وكيف يمكنك التأكد عملياً من أنها معدن الأباتيت باستخدام لوح المخدش الخزفي؟",
            modelAnswer = "1. طبقاً لمقياس موهس: الكالسيت صلادته (3) والفلوريت (4)، بينما مسمار الصلب صلادته تقريباً (5.5). إذن صلادة العينة المجهولة تقع بين أكثر من 4 وأقل من 5.5، وتطابق صلادة الأباتيت (5).\n2. للتأكد عملياً: يتم حك العينة المجهولة بلوح المخدش الخزفي (صلادته 6.5)، فبما أن لوح المخدش أصلد من العينة (6.5 > 5)، سيقوم بخدشها بسهولة وينتج مسحوقاً أبيض يمثل لون مخدش الأباتيت المميز.",
            gradingRubric = "تحديد مدى الصلادة بدقة ومقارنتها بمقياس موهس (درجة) + شرح استخدام لوح المخدش الخزفي ومقارنة صلادته بنتيجة المسحوق (درجة)."
        ),

        // ========================= 5. اللغة العربية (ARABIC) =========================
        // الاختياري (50%)
        ThanaweyaQuestion(
            id = "ara_mcq_1",
            subjectId = "arabic",
            subjectName = "اللغة العربية",
            chapterName = "الوحدة الثانية: المشتقات وإعمالها",
            lessonName = "إعمال اسم الفاعل وصيغ المبالغة",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "متوسط",
            source = "كتاب الأضواء والامتحان",
            questionText = "قال الشاعر: «أَلِفْتُ مُنْذُ صِبَايَ الصَّدْقَ سَاجِيَةً ... وَمَا أَنَا بِخَائِفٍ خِدَاعَ اللَّئِيمِ»\nإعراب كلمة (خِداعَ) في الشطر الثاني هو:",
            options = listOf("مضاف إليه مجرور بالكسرة", "مفعول به لاسم الفاعل منصوب بالفتحة", "فاعل لاسم الفاعل مرفوع بالضمة", "خبر ثانٍ للمبتدأ منصوب بالفتحة"),
            correctOptionIndex = 1,
            modelAnswer = "(بخائفٍ): خائف اسم فاعل عامل عمل فعله لاعتماده على نفي (ما). الباء حرف جر زائد في خبر ما. المعنى: وما أنا أخافُ خداعَ، إذن (خداعَ): مفعول به لاسم الفاعل (خائف) منصوب وعلامة نصبه الفتحة الظاهرة."
        ),
        ThanaweyaQuestion(
            id = "ara_mcq_2",
            subjectId = "arabic",
            subjectName = "اللغة العربية",
            chapterName = "الوحدة الثالثة: المبتدأ والخبر ونواسخه",
            lessonName = "كاد وأخواتها وأحكام اقتران الخبر بأن",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "صعب",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "«أوشك الطلاب أن يحققوا آمالهم» - عند وضع الفعل (شرع) بدلاً من الفعل (أوشك) تصبح الجملة صحيحة:",
            options = listOf("شرع الطلاب أن يحققوا آمالهم", "شرع الطلاب يحققون آمالهم", "شرع الطلاب يحققوا آمالهم", "شرع الطلاب أن يحققون آمالهم"),
            correctOptionIndex = 1,
            modelAnswer = "الفعل (شرع) من أفعال الشروع، وحكم اقتران خبر أفعال الشروع بـ (أن) يمتنع تماماً. ويكون خبرها جملة فعلية فعلها مضارع مرفوع، فيكون الصواب: «شرع الطلابُ يحققون آمالهم» بثبوت النون."
        ),
        // المقالي (50%)
        ThanaweyaQuestion(
            id = "ara_essay_1",
            subjectId = "arabic",
            subjectName = "اللغة العربية",
            chapterName = "الوحدة الرابعة: المنصوبات والمفاعيل",
            lessonName = "المفعول المطلق والنائب عنه والمفعول لأجله",
            type = QuestionType.ESSAY,
            difficulty = "متوسط",
            source = "كتاب الامتحان والوزارة",
            questionText = "اجعل كلمة (إخلاص) في ثلاث جمل مفيدة من إنشائك بحيث تكون في الأولى: مفعولاً مطلقاً، وفي الثانية: نائباً عن المفعول المطلق، وفي الثالثة: مفعولاً لأجله، مع ضبط الكلمة بالشكل في كل جملة.",
            modelAnswer = "1. مفعول مطلق: «أخلَصَ العالمُ لوطنهِ إخلاصاً عظيماً» (منصوب بالفتحة، مصدر صريح من نفس لفظ الفعل قبله).\n2. نائب عن المفعول المطلق: «يُحبُّ المصريُّ وطنهُ كلَّ الإخلاصِ» أو «أحببتُ وطني غايةَ الإخلاصِ» (مضاف إلى المصدر) أو «أخلَصتُ في عملي ذاكَ الإخلاصَ».\n3. مفعول لأجله: «يُواصلُ الطالبُ مذاكرتهُ إخلاصاً في طلبِ العلمِ» (منصوب بالفتحة، يبين سبب وقوع الفعل).",
            gradingRubric = "صياغة جملة المفعول المطلق وضبطها (درجة) + صياغة جملة النائب عن المفعول المطلق والمفعول لأجله وضبطهما (درجة)."
        ),
        ThanaweyaQuestion(
            id = "ara_essay_2",
            subjectId = "arabic",
            subjectName = "اللغة العربية",
            chapterName = "الأدب: المدارس الشعرية والقصة والرواية",
            lessonName = "مدرسة الإحياء والبعث وجيل التطوير",
            type = QuestionType.ESSAY,
            difficulty = "صعب",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "ناقش العوامل التي هيأت لتلاميذ البارودي (شوقي وحافظ وإخوانهما) تطوير الاتجاه الإحيائي في الشعر العربي مع ذكر سمات هذا التطوير.",
            modelAnswer = "العوامل:\n1. الانفتاح على الثقافة الغربية بمعرفة اللغات الأجنبية ومخالطة الأجانب وقراءة المترجمات إثر الاحتلال البريطاني.\n2. النضال الوطني ورسوخ فكرة الجامعة الإسلامية كرمز لوحدة المسلمين.\n3. مواقفهم من قضايا العصر: كالقصر الحاكم، تحرير المرأة، حرية الصحافة، وإنشاء الجامعة المصرية.\n\nسمات التطوير:\n1. عالجوا مشكلات مجتمعهم وما يتصل بالشؤون الخارجية للعالم الإسلامي.\n2. أفسحوا المجال لمزيد من التجارب الذاتية، ونوّعوا في الأغراض وابتكروا المعاني.\n3. واءموا بين اتجاهين: الأخذ من التراث العربي الأصيل والالتفات إلى ثقافة العصر.\n4. سهلت أساليبهم بسبب ارتباطهم بالصحافة اليومية.",
            gradingRubric = "ذكر عاملين من عوامل التطوير (درجة) + ذكر سمتين رئيسيتين من سمات التطوير (درجة)."
        ),

        // ========================= 6. اللغة الإنجليزية (ENGLISH) =========================
        // الاختياري (50%)
        ThanaweyaQuestion(
            id = "eng_mcq_1",
            subjectId = "english",
            subjectName = "اللغة الإنجليزية",
            chapterName = "Unit 1-2: Past & Perfect Tenses",
            lessonName = "Past Perfect vs Past Simple & Time Linkers",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "متوسط",
            source = "Longman & Thanaweya Ministerial Samples",
            questionText = "No sooner _______ the crucial laboratory experiment than the unexpected power cut occurred.",
            options = listOf("had the scientist started", "the scientist had started", "did the scientist start", "the scientist started"),
            correctOptionIndex = 0,
            modelAnswer = "When a sentence begins with negative or restrictive adverbs like 'No sooner', 'Hardly', or 'Scarcely', inversion is mandatory: auxiliary verb + subject + main verb (Had + subject + P.P.). Therefore, 'had the scientist started' is the correct construction."
        ),
        ThanaweyaQuestion(
            id = "eng_mcq_2",
            subjectId = "english",
            subjectName = "اللغة الإنجليزية",
            chapterName = "Unit 7-8: Conditionals & Wish",
            lessonName = "Third Conditional and Inversion (Had)",
            type = QuestionType.MULTIPLE_CHOICE,
            difficulty = "صعب",
            source = "Al-Adwaa & Al-Moaser Thanaweya",
            questionText = "_______ for her remarkable persistence and diligent study habits, she wouldn't have achieved top ranks in the nationwide secondary exams.",
            options = listOf("Had it not been", "Were it not", "If it hasn't been", "Without being"),
            correctOptionIndex = 0,
            modelAnswer = "'Had it not been for + noun phrase' is the formal inverted structure replacing 'If it hadn't been for' in the Third Conditional to express past hypothetical events and their past consequences ('wouldn't have achieved')."
        ),
        // المقالي (50%)
        ThanaweyaQuestion(
            id = "eng_essay_1",
            subjectId = "english",
            subjectName = "اللغة الإنجليزية",
            chapterName = "Advanced Vocabulary & Translation Skills",
            lessonName = "Contextual Academic Translation into English",
            type = QuestionType.ESSAY,
            difficulty = "متوسط",
            source = "امتحانات الوزارة الاسترشادية",
            questionText = "ترجم الجملة الآتية إلى لغة إنجليزية سليمة وأسلوب أكاديمي دقيق:\n«إن البحث العلمي هو حجر الزاوية الحقيقي لتحقيق التنمية المستدامة، وبناء اقتصاد وطني قوي قائم على المعرفة والابتكار.»",
            modelAnswer = "Model Translation:\n\"Scientific research is the true cornerstone of achieving sustainable development and building a robust national economy based on knowledge and innovation.\"\n\nAlternative acceptable phrases:\n- cornerstone / bed-rock / fundamental pillar\n- sustainable development\n- robust / strong knowledge-based economy.",
            gradingRubric = "صحة استخدام القواعد وتركيب الجملة الإنجليزية (درجة) + دقة المصطلحات العلمية: sustainable development, cornerstone, knowledge-based economy (درجة)."
        ),
        ThanaweyaQuestion(
            id = "eng_essay_2",
            subjectId = "english",
            subjectName = "اللغة الإنجليزية",
            chapterName = "Advanced Vocabulary & Translation Skills",
            lessonName = "Critical Reading Comprehension and Analytical Expression",
            type = QuestionType.ESSAY,
            difficulty = "صعب",
            source = "كتاب الامتحان والوزارة",
            questionText = "In about 4-5 well-structured sentences, write your critical view on: \"How artificial intelligence can revolutionize modern secondary education without eliminating the indispensable role of inspirational teachers.\"",
            modelAnswer = "Artificial intelligence offers customized learning paths and instant feedback, allowing high school students to master complex scientific concepts at their own individual pace. It automates repetitive grading tasks, freeing educators to focus on mentoring and moral guidance. However, machines cannot replicate human empathy, motivational passion, or ethical inspiration. Therefore, the future of optimal education lies in an integrated synergy between cutting-edge AI tools and creative, inspirational human teachers.",
            gradingRubric = "Coherence and clear logical argumentation (1 mark) + Accurate grammatical structures and advanced academic vocabulary (1 mark)."
        )
    )

    fun getQuestions(
        subjectId: String,
        chapter: String? = null,
        difficulty: String? = null,
        count: Int = 10
    ): List<ThanaweyaQuestion> {
        val pool = questions.filter { q ->
            val matchesSubject = q.subjectId == subjectId
            val matchesChapter = chapter == null || chapter == "الكل" || q.chapterName == chapter
            val matchesDiff = difficulty == null || difficulty == "الكل" || q.difficulty == difficulty
            matchesSubject && matchesChapter && matchesDiff
        }

        if (pool.isEmpty()) {
            return questions.filter { it.subjectId == subjectId }.take(count)
        }

        // Ensure 50% Multiple Choice and 50% Essay
        val mcqList = pool.filter { it.type == QuestionType.MULTIPLE_CHOICE }.shuffled()
        val essayList = pool.filter { it.type == QuestionType.ESSAY }.shuffled()

        val halfCount = count / 2
        val chosenMcq = mcqList.take(halfCount)
        val chosenEssay = essayList.take(count - chosenMcq.size)

        val combined = (chosenMcq + chosenEssay).shuffled()
        return if (combined.isNotEmpty()) combined else pool.take(count)
    }
}
