package com.example.data

data class DictionaryEntry(
    val wordEn: String,
    val wordAr: String,
    val category: String, // أحياء، كيمياء، فيزياء، جيولوجيا، رياضيات، إنجليزي، أكاديمي
    val partOfSpeech: String,
    val definitionAr: String,
    val exampleEn: String,
    val exampleAr: String
)

object DictionaryProvider {
    val categories = listOf("الكل", "أحياء", "كيمياء", "فيزياء", "جيولوجيا", "رياضيات", "لغة إنجليزية", "مصطلحات أكاديمية")

    val entries = listOf(
        // الأحياء (Biology)
        DictionaryEntry("Photosynthesis", "البناء الضوئي", "أحياء", "عملية حيوية", "عملية تقوم بها النباتات لتحويل الطاقة الضوئية إلى طاقة كيميائية وغذاء", "Plants produce oxygen through photosynthesis.", "تنتج النباتات الأكسجين من خلال عملية البناء الضوئي."),
        DictionaryEntry("Homeostasis", "الاتزان الداخلي", "أحياء", "مفهوم فسيولوجي", "قدرة الكائن الحي على الحفاظ على بيئة داخلية ثابتة ومستقرة", "Homeostasis regulates human body temperature.", "ينظم الاتزان الداخلي درجة حرارة جسم الإنسان."),
        DictionaryEntry("Chromosome", "كروموسوم / صبغي", "أحياء", "تركيب خلوي", "تركيب خيطي داخل نواة الخلية يحمل المادة الوراثية والجينات", "Humans have 23 pairs of chromosomes.", "يمتلك الإنسان 23 زوجاً من الكروموسومات."),
        DictionaryEntry("Enzyme", "إنزيم", "أحياء", "محفز حيوي", "بروتين يعمل كمحفز بيولوجي لتسريع التفاعلات الحيوية في الخلية", "Enzymes break down food during digestion.", "تعمل الإنزيمات على تفكيك الطعام أثناء عملية الهضم."),
        DictionaryEntry("Mitochondria", "الميتوكوندريا", "أحياء", "عضية خلوية", "عضيات خلوية مسؤولة عن إنتاج وتوليد طاقة الخلية ATP", "Mitochondria are known as the powerhouses of the cell.", "تُعرف الميتوكوندريا بأنها مراكز توليد الطاقة في الخلية."),
        DictionaryEntry("Antibody", "جسم مضاد", "أحياء", "جهاز مناعي", "بروتين مناعي يفرزه الجهاز المناعي للتعرف على الأجسام الغريبة ومهاجمتها", "Antibodies defend against viral infections.", "تدافع الأجسام المضادة عن الجسم ضد العدوى الفيروسية."),
        DictionaryEntry("Mutation", "طفرة جينية", "أحياء", "وراثة", "تغير مفاجئ ودائم في تسلسل النيوكليوتيدات في الحمض النووي DNA", "Gene mutations can cause hereditary traits.", "يمكن أن تؤدي الطفرات الجينية إلى صفات وراثية جديدة."),

        // الكيمياء (Chemistry)
        DictionaryEntry("Catalyst", "عامل حفاز", "كيمياء", "مصطلح كيميائي", "مادة تزيد من سرعة التفاعل الكيميائي دون أن تستهلك أو تتغير", "Iron is used as a catalyst in ammonia production.", "يستخدم الحديد كعامل حفاز في إنتاج الأمونيا."),
        DictionaryEntry("Electronegativity", "السالبية الكهربية", "كيمياء", "خاصية كيميائية", "قدرة الذرة في الجزيء على جذب إلكترونات الرابطة التساهمية نحوها", "Fluorine has the highest electronegativity.", "عنصر الفلور يمتلك أعلى سالبية كهربية في الجدول الدوري."),
        DictionaryEntry("Oxidation", "أكسدة", "كيمياء", "تفاعل كيميائي", "عملية كيميائية تفقد فيها الذرة أو الأيون إلكترونات أو يزداد عدد تأكسدها", "Rusting of iron is a common oxidation reaction.", "صدأ الحديد هو تفاعل أكسدة شائع في الطبيعة."),
        DictionaryEntry("Isomerism", "المشابهة الجزيئية / الآيزوميرية", "كيمياء", "كيمياء عضوية", "وجود أكثر من مركب له نفس الصيغة الجزيئية ولكنه يختلف في الصيغة البنائية", "Isomers share the same formula but have different structures.", "تشترك المتشابهات في نفس الصيغة ولكن تختلف في بنائها."),
        DictionaryEntry("Titration", "معايرة", "كيمياء", "تحليل كمي", "طريقة مخبرية للتحليل الكيميائي لتحديد تركيز محلول مجهول باستخدام محلول قياسي", "Titration determines the unknown acid concentration.", "تحدد المعايرة تركيز الحمض المجهول بدقة عالية."),
        DictionaryEntry("Equilibrium", "اتزان كيميائي", "كيمياء", "ديناميكا حرارية", "حالة يتساوى فيها معدل التفاعل الطردي مع معدل التفاعل العكسي", "Dynamic equilibrium occurs in reversible reactions.", "يحدث الاتزان الديناميكي في التفاعلات الانعكاسية."),

        // الفيزياء (Physics)
        DictionaryEntry("Magnetic Flux", "الفيض المغناطيسي", "فيزياء", "كمية فيزيائية", "العدد الكلي لخطوط الفيض المغناطيسي التي تمر عمودياً على مساحة معينة", "Magnetic flux is measured in Webers.", "يقاس الفيض المغناطيسي بوحدة الويبر."),
        DictionaryEntry("Resonance", "الرنين", "فيزياء", "دوائر التيار المتردد", "حالة في الدائرة تتساوى فيها المفاعلة الحثية مع السعوية وتكون المعاوقة أقل ما يمكن", "Resonance circuits are used in radio tuners.", "تستخدم دوائر الرنين في أجهزة استقبال الراديو."),
        DictionaryEntry("Photoelectric Effect", "الظاهرة الكهروضوئية", "فيزياء", "فيزياء حديثة", "انبعاث إلكترونات من سطح معدن عند سقوط ضوء ذي تردد مناسب عليه", "Einstein explained the photoelectric effect.", "فسر أينشتاين الظاهرة الكهروضوئية ونال نوبل عليها."),
        DictionaryEntry("Electromotive Force", "القوة الدافعة الكهربية (ق.د.ك)", "فيزياء", "كهربية تيارية", "الشغل الكلي المبذول لنقل شحنة مقدارها 1 كولوم في الدائرة الكهربية كاملة", "The electromotive force of the battery is 12V.", "القوة الدافعة الكهربية للبطارية تساوي 12 فولت."),
        DictionaryEntry("Refraction", "انكسار الضوء", "فيزياء", "بصريات وموجات", "انحراف مسار الشعاع الضوئي عند انتقاله بين وسطين شفافين مختلفي الكثافة", "Light refraction causes rainbows to form.", "يؤدي انكسار الضوء إلى ظهور قوس قزح في السماء."),

        // الجيولوجيا (Geology)
        DictionaryEntry("Mineral", "معدن", "جيولوجيا", "علم المعادن", "مادة طبيعية صلبة غير عضوية لها تركيب كيميائي محدد وشكل بلوري مميز", "Quartz is an abundant continental mineral.", "الكوارتز معدن متوفر بكثرة في القشرة القارية."),
        DictionaryEntry("Fault", "فالق / صدع", "جيولوجيا", "تراكيب تكتونية", "كسر أو تشقق في الكتل الصخرية تصحبه إزاحة وحركة على جانبي مستوى الفالق", "Normal faults are caused by tensional stress.", "تنشأ الفوالق العادية نتيجة قوى الشد التكتوني."),
        DictionaryEntry("Fossil", "حفرية", "جيولوجيا", "تاريخ الأرض", "بقايا أو آثار كائنات حية قديمة محفوظة في الصخور الرسوبية", "Index fossils help determine the age of rock layers.", "تساعد الحفريات المرشدة في تحديد أعمار الطبقات الصخرية."),
        DictionaryEntry("Metamorphism", "التحول الصخري", "جيولوجيا", "دورة الصخور", "تغير يطرأ على الصخر الأصلي نتيجة تعرضه لحرارة عالية أو ضغط شديد أو كلاهما", "Marble is formed through metamorphism of limestone.", "يتكون الرخام من خلال تحول الحجر الجيري."),
        DictionaryEntry("Plate Tectonics", "الصفائح التكتونية", "جيولوجيا", "حركات أرضية", "نظرية تفسر حركة أجزاء الغلاف الصخري للأرض وتكوين الجبال والزلازل", "Plate tectonics explains earthquakes and volcanoes.", "تفسر حركة الصفائح التكتونية حدوث الزلازل والبراكين."),

        // الرياضيات (Mathematics)
        DictionaryEntry("Derivative", "مشتقة / تفاضل", "رياضيات", "حساب التفاضل", "معدل تغير دالة رياضية بالنسبة إلى أحد متغيراتها", "The derivative of velocity with respect to time is acceleration.", "مشتقة السرعة بالنسبة للزمن هي العجلة."),
        DictionaryEntry("Integration", "تكامل", "رياضيات", "حساب التكامل", "عملية رياضية عكسية للتفاضل تُستخدم لحساب المساحات والحجوم", "Integration helps calculate areas under curves.", "يساعد التكامل في حساب المساحات تحت المنحنيات."),
        DictionaryEntry("Probability", "احتمال", "رياضيات", "إحصاء واحتمالات", "مقياس رياضي لمدى إمكانية وقوع حدث معين بين 0 و 1", "The probability of rolling a six is one-sixth.", "احتمال الحصول على الرقم 6 في النرد هو سدس."),
        DictionaryEntry("Matrix", "مصفوفة", "رياضيات", "جبر خطي", "ترتيب مستطيل للأعداد في صفوف وأعمدة يُستخدم لحل المعادلات الخطية", "Matrices are used to solve systems of linear equations.", "تستخدم المصفوفات لحل أنظمة المعادلات الخطية."),

        // اللغة الإنجليزية والترجمة (English)
        DictionaryEntry("Hypothesis", "فرضية", "لغة إنجليزية", "مصطلح أكاديمي", "تفسير مقترح ومبدئي لظاهرة يحتاج إلى اختبار وتجربة", "The researcher proposed an innovative hypothesis.", "اقترح الباحث فرضية مبتكرة ومتميزة."),
        DictionaryEntry("Phenomenon", "ظاهرة", "لغة إنجليزية", "اسم علمي", "حدث أو حقيقة ملحوظة في الطبيعة أو المجتمع قابلة للدراسة", "Global warming is a critical environmental phenomenon.", "الاحتباس الحراري ظاهرة بيئية خطيرة."),
        DictionaryEntry("Crucial", "حاسم / جوهري", "لغة إنجليزية", "صفة (Adjective)", "ذو أهمية بالغة وأثر قاطع في النتيجة النهائية", "Time management is crucial for exam success.", "تنظيم الوقت أمر حاسم للنجاح في الامتحانات."),
        DictionaryEntry("Substantial", "كبير / ذو شأن", "لغة إنجليزية", "صفة (Adjective)", "كمية أو درجة كبيرة وذات قيمة ملموسة", "He made substantial progress in physics.", "أحرز تقدماً كبيراً وملحوظاً في مادة الفيزياء."),
        DictionaryEntry("Comprehension", "فهم / استيعاب", "لغة إنجليزية", "مهارة لغوية", "القدرة العقلية على إدراك وفهم المعاني والنصوص", "Reading comprehension requires careful analytical thinking.", "استيعاب القراءة يتطلب تفكيراً تحليلياً متأنياً."),

        // مصطلحات أكاديمية عامة (General Academic)
        DictionaryEntry("Methodology", "منهجية البحث", "مصطلحات أكاديمية", "مصطلح علمي", "نظام الطرق والأسس المتبعة لإنجاز دراسة أو بحث علمي", "The scientific methodology ensures accurate results.", "تضمن المنهجية العلمية نتائج دقيقة وموثوقة."),
        DictionaryEntry("Empirical", "تجريبي / قائم على الملاحظة", "مصطلحات أكاديمية", "صفة علمية", "مبني على الدليل والتجربة الحسية المباشرة وليس مجرد نظرية", "Empirical evidence supports this scientific theory.", "الدليل التجريبي يعزز هذه النظرية العلمية."),
        DictionaryEntry("Analysis", "تحليل", "مصطلحات أكاديمية", "مهارة تفكير", "تفكيك الموضوع المعقد إلى عناصره الأولية لفهمه بعمق", "Data analysis revealed significant patterns.", "كشف تحليل البيانات عن أنماط مهمة ومؤثرة.")
    )

    fun search(query: String, selectedCategory: String = "الكل"): List<DictionaryEntry> {
        val q = query.trim().lowercase()
        return entries.filter { entry ->
            val matchesCategory = selectedCategory == "الكل" || entry.category == selectedCategory
            val matchesQuery = q.isEmpty() ||
                    entry.wordEn.lowercase().contains(q) ||
                    entry.wordAr.contains(q) ||
                    entry.definitionAr.contains(q) ||
                    entry.category.contains(q)
            matchesCategory && matchesQuery
        }
    }
}
