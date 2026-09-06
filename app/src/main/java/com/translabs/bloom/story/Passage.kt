package com.translabs.bloom.story

data class Passage(val title: String, val focus: String, val text: String)

object CuratedPassages {
    val all = listOf(
        Passage("The Glass City", "Upward pitch glides 🎢",
            "Good morning! Did you sleep well? I had the most wonderful dream — we were flying " +
                    "over a city made of glass, and every window sang back to us! Can you believe it? " +
                    "Neither can I! But oh, what a lovely feeling it was, soaring high above the shiny " +
                    "streets. Tell me, if you could fly, where would you go first? Would you visit the " +
                    "moon? Or maybe just float above the fluffy clouds?"),

        Passage("Tea Time", "Forward buzz 🐝",
            "The kettle hums a little song: mmmmm. The cups are waiting in a row, neat and tiny. " +
                    "Which one shall we pick today? The pink one, of course! Softly now... the water is " +
                    "warm, the honey is sweet, and there is absolutely no rush at all. Listen to the " +
                    "gentle clink of the spoon... clink, clink. Let the warmth melt all your worries away. " +
                    "Mmm, doesn't that smell nice?"),

        Passage("The Brave Little Sprout", "High vowels ✨",
            "Once there was a tiny, teeny sprout who wanted to see the deep blue sea. 'Just one " +
                    "more stretch,' she whispered, 'and one more after that!' Up she grew, bright and " +
                    "keen, reaching her green leaves to the sky. The sun smiled down and said: 'Well, " +
                    "hello there, little one! You are free to be exactly what you please.' And she beamed " +
                    "with delight, shining like a little emerald in the breeze."),

        Passage("Questions, Questions!", "Melodic speech 🎶",
            "Is it raining? Is it snowing? Or is the sky just being dramatic again? What do you " +
                    "think? Tell me everything — the long version, the short version, the silly version! " +
                    "Oh, and one more thing: did you smile today? Because you have the most wonderful " +
                    "smile, and it makes the whole world a little bit brighter. Isn't that just the " +
                    "nicest thing you've heard all day?"),

        Passage("The Compliment Garden", "Light vocal weight 🌸",
            "My voice is mine, and it is beautiful. Does it get stronger every day? It does! It " +
                    "really does! I am proud of it, I am proud of me, and honestly? We are just getting " +
                    "started. Every time I speak, a new little flower blooms in my garden. Soft, gentle, " +
                    "and strong. I am enough, exactly as I am right now, in this exact moment. And I am " +
                    "so very glad that I am here."),

        Passage("Storm & Kitten", "Dynamic contrast 🌩️",
            "BOOM went the thunder! The wind howled, WHOOSH, shaking the big oak trees! But " +
                    "then... tiniest of all... mew went the kitten. Big and small, loud and soft, the " +
                    "storm and the kitten shared the same sky. The thunder rumbled low, but the kitten " +
                    "purred high and sweet. Which one are you today? Why not both? You can be as big as " +
                    "a storm, and as soft as a kitten."),
    )
}