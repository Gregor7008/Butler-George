Hey!
Thanks for considering to translate this bot to another language.
Before you start though, please read the following notes carefully!

1. We currently have 5 languages implemented in the bot to choose from. 4 of those languages need translation.
   You can view our current progress in "Progress.txt" right next to this file.
   The folders for each language are already created though the files not translated.
   If you create a folder for a different language, please copy the "en" folder and rename the copy to your language.
   Please note: The language will not be available as soon as the folder is created, as we have add it to the bots list!

2. The bot is written in the programming language "Java", which makes handling plain text really easy.
   But as soon as it gets to simple symbols like '+', '/' or '"' Java struggles.
   I found a workaround for that though, so if you are someone who knows Java: Don't worry about it.

3. Javas file-reading process works with keywords. You will therefore notice the following structure in all files:
   -> keyword=a_lot_of_text_here
   For everything to work correctly: DO NOT TOUCH the keyword and the '='. Only translate what's written behind the '='!
   Please note: Java will only read the text BEHIND the keyword, not the text in a following line!

4. Because the bot needs two text lines for one answer, it needed a separation symbol.
   For that I choose ';'. You'll notice them not far behind the '='.
   You may translate everything before and behind the ';'. DO NOT TOUCH the ';' itself, as well as the space behind it!

5. Because some answers contain variating content, a user name for example, it needed a variable declaration.
   For that I choose "{variable}". If it's a user name, it's probably going to be named like that: "{username}".
   You may translate everything before and behind the variable. DO NOT TOUCH the variable itself!

6. As every programming language, Java has a problem with formatting concerning plain text.
   Oracle, the developers of Java, therefore introduced expressions like "\n" or "\r" for text formatting.
   "\n" will create a new line, "\r" will reset the current writing position to the start like pressing "Pos 1"
   in the middle of a line whilst writing.
   For the english version I used "\n" sometimes, when the line got too long, or the topic changed drastically.
   You may use "\n" at your own discretion.

7. Now something about Discord: Discord does not use Unicode to express their emojis.
   (Unicode is an international system, where all characters got a code,
   so they can be recognized by everyone, independent of the language and the system. Looks like this: U+2764)
   Discord instead uses expressions like ":sweat_smile:". To now use emojis in the text, we have to just write
   the expression in the text and it'll be replaced automatically.
   You'll see those expressions used in nearly every message and recognize them in the files as well.
   You of course can touch that stuff, but I tried to keep it even by always using this system:
   -> keyword=a_bit_of_text; :emoji_expression: | a_lot_of_text
   You notice me using '|' after every emoji, but that may be changed at your own discretion!

8. DO NOT CHANGE THE FOLDER STRUCTURE, I DARE YOU!

9. Please update the language you are working on in "Progress.txt" every time you push to GitHub or send an update to someone!

Now that that's all said, you can safely start translating.
Thanks again for taking yourself time to do this. If it's okay for you,
I will credit you in the bot whenever your language is used. Please let me know by typing your name in "Progress.txt"
next to your language!
Best regards
Gregor7008#6565