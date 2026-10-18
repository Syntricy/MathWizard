import java.util.Random;
import java.util.Scanner;

public class MathWizard {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";

    static String[] monsterNames = {"Slime", "Goblin", "Math Dragon"};
    static int[] monsterStartHp = {30, 50, 80};
    static int[] monsterDamage = {5, 8, 12};

    static int playerHp = 50;
    static int score = 0;
    static int streak = 0;          
    static int questionsAsked = 0;  
    static int correctAnswers = 0;

    static final int WON = 0;
    static final int DIED = 1;
    static final int QUIT = 2;

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Random rand = new Random();
        clearScreen();
        printTitle();
        printRules();
        
        System.out.print("Press ENTER to begin (or Q to quit): ");
        String start = scan.hasNextLine() ? scan.nextLine().trim() : "Q";
        if (start.equalsIgnoreCase("Q")) {
            System.out.println("Goodbye, warrior!");
        } else {
            playGame(scan, rand);
        }
        scan.close();
    }

    public static void playGame(Scanner scan, Random rand) {
        int result = WON;
        for (int i = 0; i < 3 && result == WON; i++) {
            result = fightMonster(i, scan, rand);
        }
        
        clearScreen();
        switch (result) {
            case WON:
                int bonus = playerHp * 2;
                score = score + bonus;
                printVictory();
                System.out.println(GREEN + "You survived with " + playerHp + " HP. HP bonus: +" + bonus + RESET);
                break;
            case DIED:
                printGameOver();
                break;
            default:
                System.out.println("\nYou left the arena. Thanks for playing!");
                break;
        }
        printSummary();
    }

    public static int fightMonster(int index, Scanner scan, Random rand) {
        String name = monsterNames[index];
        int monsterHp = monsterStartHp[index];
        int damage = monsterDamage[index];
        int maxHp = monsterStartHp[index];

        while (monsterHp > 0 && playerHp > 0) {
            clearScreen();
            printMonster(index);
            printHealthBar(monsterHp, maxHp, name);
            
            System.out.println("\n" + CYAN + "=== YOUR STATS ===" + RESET);
            System.out.println("HP: " + playerHp + " | Score: " + score + " | Streak: " + streak + "/3");
            System.out.println("------------------\n");

            String[] problem = generateMathProblem(rand, index);
            String input = getAnswer(scan, problem[0]);
            
            if (input.equalsIgnoreCase("Q")) {
                return QUIT;
            }
            
            int x = Integer.parseInt(input);
            int correct = Integer.parseInt(problem[1]);
            questionsAsked++;

            System.out.println(); 
            
            if (x == correct) {
                correctAnswers++;
                score = score + 10;
                System.out.println(GREEN + "Correct! The answer was " + correct + "." + RESET);
                int hit = playerAttack(x);
                monsterHp = monsterHp - hit;
                System.out.println(GREEN + "You hit the " + name + " for " + hit + " damage!" + RESET);
                
                if (streak == 3) {
                    System.out.println(YELLOW + "Your NEXT correct answer will be a CRITICAL HIT!" + RESET);
                }
                
                if (monsterHp > 0) {
                    enemyTurn(name, damage);
                }
            } else {
                System.out.println(RED + "Wrong! The answer was " + correct + ". Your streak is reset." + RESET);
                streak = 0;
                enemyTurn(name, damage);
            }

            System.out.print("\nPress ENTER to continue...");
            scan.nextLine();
        }

        if (playerHp <= 0) {
            return DIED;
        }
        
        clearScreen();
        printMonster(index);
        printHealthBar(0, maxHp, name);
        score = score + 50;
        System.out.println(GREEN + "\nYou defeated the " + name + "! +50 points" + RESET);
        System.out.print("\nPress ENTER to continue...");
        scan.nextLine();
        
        return WON;
    }

    public static void printHealthBar(int currentHP, int maxHP, String monsterName) {
        int barLength = 20;
        int filled = (int) (((double) currentHP / maxHP) * barLength);
        if (filled < 0) filled = 0;
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        String color = (currentHP < maxHP * 0.3) ? RED : RESET;
        System.out.println("\n  " + monsterName.toUpperCase() + " | HP: " + color + "[" + bar.toString() + "]" + RESET + " " + currentHP + "/" + maxHP);
    }

    public static String getAnswer(Scanner scan, String questionText) {
        String border = CYAN + "+-----------------------------------------+" + RESET;
        String qLine = "| Solve for x:  " + questionText;
        while (qLine.length() < 41) { qLine += " "; }
        qLine += "|";

        System.out.println(border);
        System.out.println(CYAN + qLine + RESET);
        System.out.println(border);

        while (true) {
            System.out.print("| What is x? > ");
            if (!scan.hasNextLine()) {
                return "Q";
            }
            String line = scan.nextLine().trim();
            System.out.println(border);

            if (line.equalsIgnoreCase("Q") || isValidInteger(line)) {
                return line;
            }
            System.out.println(RED + "Invalid input! Please type a number." + RESET);
        }
    }

    public static int playerAttack(int x) {
        int damage = (x * 2) + 5;
        if (streak >= 3) {
            System.out.println(YELLOW + "*** CRITICAL HIT! STREAK BONUS ACTIVATED! ***" + RESET);
            damage = damage * 2;
            streak = 0;
        } else {
            streak++;
        }
        return damage;
    }

    public static void enemyTurn(String name, int damage) {
        playerHp = playerHp - damage;
        if (playerHp < 0) {
            playerHp = 0;
        }
        System.out.println(RED + "The " + name + " attacks you for " + damage + " damage!" + RESET);
    }

    public static String[] generateMathProblem(Random rand, int monsterIndex) {
        int x;
        String question;

        if (monsterIndex == 0) {
            int num1 = rand.nextInt(10) + 1;
            int num2 = rand.nextInt(10) + 1;
            if (rand.nextBoolean()) {
                int sum = num1 + num2;
                x = sum - num1;
                question = "x + " + num1 + " = " + sum;
            } else {
                x = num2 + num1;
                question = "x - " + num1 + " = " + num2;
            }
        } else if (monsterIndex == 1) {
            int num1 = rand.nextInt(8) + 2; 
            int num2 = rand.nextInt(8) + 2; 
            if (rand.nextBoolean()) {
                x = num2;
                int product = num1 * num2;
                question = num1 + " * x = " + product;
            } else {
                x = num1 * num2;
                question = "x / " + num1 + " = " + num2;
            }
        } else {
            int type = rand.nextInt(3);
            if (type == 0) {
                int num1 = rand.nextInt(5) + 2; 
                x = rand.nextInt(5) + 1; 
                int num2 = rand.nextInt(10) + 1;
                int answer = (num1 * x) + num2;
                question = num1 + "x + " + num2 + " = " + answer;
            } else if (type == 1) {
                int num1 = rand.nextInt(5) + 2; 
                x = rand.nextInt(8) + 2;
                int num2 = rand.nextInt(10) + 1;
                int answer = (num1 * x) - num2;
                question = num1 + "x - " + num2 + " = " + answer;
            } else {
                int num1 = rand.nextInt(4) + 2;
                x = rand.nextInt(4) + 1;
                int negAnswer = -(num1 * x);
                question = "-" + num1 + " * x = " + negAnswer;
            }
        }
        return new String[] {question, String.valueOf(x)};
    }

    public static boolean isValidInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void printSummary() {
        System.out.println(CYAN + "\n===== FINAL RESULTS =====" + RESET);
        System.out.println("Final Score: " + score);
        if (questionsAsked > 0) {
            double accuracy = (double) correctAnswers * 100 / questionsAsked;
            System.out.printf("Accuracy: %d/%d correct (%.1f%%)%n", correctAnswers, questionsAsked, accuracy);
        }
    }

    public static void printMonster(int index) {
        switch (index) {
            case 0: printSlime(); break;
            case 1: printGoblin(); break;
            default: printDragon(); break;
        }
    }

    public static void printRules() {
        System.out.println("OBJECTIVE: Defeat the Slime, the Goblin, and the Math Dragon!");
        System.out.println("  - Solve the algebra problem to deal damage: (x * 2) + 5");
        System.out.println("  - Wrong answer: the monster hits you.");
        System.out.println("  - STREAK BONUS: 3 correct in a row = next hit is a CRITICAL (double damage)!");
        System.out.println("  - The math gets HARDER with each monster!");
        System.out.println("  - You start with " + playerHp + " HP. Type Q at any prompt to quit.\n");
    }

    // Prints the Title screen inside a unified CYAN container box.
    public static void printTitle() {
        System.out.println(CYAN);
        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("|                                                                          |");
        System.out.println("|                              *       .     +   *   +     .       *       |");
        System.out.println("|                                                    ^                     |");
        System.out.println("|                                                  /   \\                   |");
        System.out.println("|                                                 /  *  \\                  |");
        System.out.println("|                                                / . * . \\                 |");
        System.out.println("|                                               /    o    \\                |");
        System.out.println("|                                              /===========\\               |");
        System.out.println("|                 .-~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-.                  |");
        System.out.println("|                 '-._________________________________.-'                  |");
        System.out.println("|                      __  __     _     _____  _   _                       |");
        System.out.println("|                     |  \\/  |   / \\   |_   _|| | | |                      |");
        System.out.println("|                     | |\\/| |  / _ \\    | |  | |_| |                      |");
        System.out.println("|                     | |  | | / ___ \\   | |  |  _  |                      |");
        System.out.println("|                     |_|  |_|/_/   \\_\\  |_|  |_| |_|                      |");
        System.out.println("|              __        __ ___  _____    _     ____   ____                |");
        System.out.println("|              \\ \\      / /|_ _||__  /   / \\   |  _ \\ |  _ \\               |");
        System.out.println("|               \\ \\ /\\ / /  | |   / /   / _ \\  | |_) || | | |              |");
        System.out.println("|                \\ V  V /   | |  / /_  / ___ \\ |  _ < | |_| |              |");
        System.out.println("|                 \\_/\\_/   |___|/____|/_/   \\_\\|_| \\_\\|____/               |");
        System.out.println("|           *-----=======>  M a t h W i z a r d  <=======-----*            |");
        System.out.println("|                                                                          |");
        System.out.println("+--------------------------------------------------------------------------+" + RESET);
    }

    // Prints the Slime inside a unified GREEN container box.
    public static void printSlime() {
        System.out.println(GREEN);
        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("|        .      .           .       _.-------._       .           .      |");
        System.out.println("|        .. .       .   *       _.-'           '-._       *   .          |");
        System.out.println("|          ..               .-'                   '-.                    |");
        System.out.println("|            .            .'     *      O      *     '.            .     |");
        System.out.println("|                        /   .                     .   \\                 |");
        System.out.println("|          .            |         .-\"-.       .-\"-.     |                |");
        System.out.println("|                       |        / (O) \\     / (O) \\    |          .     |");
        System.out.println("|                       |        |     |     |     |    |                |");
        System.out.println("|         .       .     |        \\ '-' /     \\ '-' /    |      .         |");
        System.out.println("|                       |         '---'       '---'     |                |");
        System.out.println("|                       |                               |                |");
        System.out.println("|              *        |     \\                   /     |        *       |");
        System.out.println("|                       \\      '.__           __.'      /                |");
        System.out.println("|                        '.        /vVvVvVvVvV\\       .'                 |");
        System.out.println("|                          \\       |          |      /                   |");
        System.out.println("|          .         .      '.     \\^^^^^^^^^^/    .'      .             |");
        System.out.println("|                         _.-'      '--------'      '-._                 |");
        System.out.println("|                       .'     o     ___..___           '.               |");
        System.out.println("|                      /   O       .'   __   '.      O    \\              |");
        System.out.println("|                     |           /   /`  `\\   \\           |             |");
        System.out.println("|                     |     .    |    | () |    |    .     |             |");
        System.out.println("|           __.._.._   \\   /      \\   \\____/   /      \\   /  _..__.._    |");
        System.out.println("|     _.--\"\"        \"\"-.|        '.        .'        |.'.-\"\"         \"   |");
        System.out.println("|     __.._..__.._..__..'-.________'------'________.-'__..__..__.._.._   |");
        System.out.println("|                                                                        |");
        System.out.println("|             SLIME  |  HP: 30  |  DMG: 5  |  SPECIAL: OOZE             |");
        System.out.println("|                                                                        |");
        System.out.println("+--------------------------------------------------------------------------+" + RESET);
    }

    // Prints the Goblin inside a unified YELLOW container box.
    public static void printGoblin() {
        System.out.println(YELLOW);
        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("|                                  __,---.__                             |");
        System.out.println("|                               .-'         '-.                          |");
        System.out.println("|                             .'               '.                        |");
        System.out.println("|                             |                 |                        |");
        System.out.println("|                   __      __|    _       _    |__      __              |");
        System.out.println("|                .-'  '-..-'  |   / \\     / \\   |  '-..-'  '-.           |");
        System.out.println("|              .'    _..-     |   >O<     >O<   |     -.._    '.         |");
        System.out.println("|             /  _.-'         |                 |         '-._  \\        |");
        System.out.println("|            | .'             \\      /^\\        /             '. |       |");
        System.out.println("|            |/                \\    |\\_/|      /                \\|       |");
        System.out.println("|                               \\   \\_-_/     /                          |");
        System.out.println("|                                | \\_____,/  |                           |");
        System.out.println("|                                | |vVvVv|   |                           |");
        System.out.println("|                                | | ^^^ |   |                           |");
        System.out.println("|                                |  '---'    |                           |");
        System.out.println("|                                 \\         /                            |");
        System.out.println("|                               ___'-.....-'___                          |");
        System.out.println("|                           .-'` / `\"---\"` \\   `'-.                      |");
        System.out.println("|                         /`    |           |      `\\                    |");
        System.out.println("|                        /   /|  \\         /  |\\     \\                   |");
        System.out.println("|                       |   | |   |       |   | |     |                  |");
        System.out.println("|                       |   | |   |       |   | |     |                  |");
        System.out.println("|                        \\  \\  \\  |       |  /  /    /                   |");
        System.out.println("|      __..__.._..__.._   '. '. \\ |       | / .'   .'  _..__..__..__.._  |");
        System.out.println("|      _.--\"\"          \"'-._'._\\|_|_______|_|/_.-'_.-'\"          \"\"--._  |");
        System.out.println("|      _..__..__..__..__..__..__..__..__..__..__..__..__..__..__..__.._  |");
        System.out.println("|                                                                        |");
        System.out.println("|             GOBLIN  |  HP: 50  |  DMG: 8  |  SPECIAL: STRIKE           |");
        System.out.println("|                                                                        |");
        System.out.println("+--------------------------------------------------------------------------+" + RESET);
    }

    // Prints the Math Dragon inside a unified RED container box.
    public static void printDragon() {
        System.out.println(RED);
        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("|                                __====-_  _-====__                      |");
        System.out.println("|                          _--^^^#####//    \\\\#####^^^--_                |");
        System.out.println("|                       _-^##########// (  ) \\\\##########^-_             |");
        System.out.println("|                      -############//  |\\/|  \\\\############-            |");
        System.out.println("|                    _/############//   (@::@)   \\\\############\\_        |");
        System.out.println("|                   /#############((     \\\\//     ))#############\\       |");
        System.out.println("|                  -###############\\\\    (oo)    //###############-      |");
        System.out.println("|                 -#################\\\\  / VV \\  //#################-     |");
        System.out.println("|                -###################\\\\/      \\//###################-    |");
        System.out.println("|               _#/|##########/\\\\######(   /\\   )######//\\##########|\\#_ |");
        System.out.println("|               |/ |#/\\#/\\#/\\/  \\#/\\##\\  |  |  /##/\\#/  \\/\\#/\\#/\\#| \\| |");
        System.out.println("|               `  |/  V  V  `   V  \\#\\| |  | |/#/  V   '  V  V  \\|  ' |");
        System.out.println("|                  `   `  `      `   / | |  | | \\   '      '  '   '      |");
        System.out.println("|                                   (  | |  | |  )                       |");
        System.out.println("|                                  __\\ | |  | | /__                      |");
        System.out.println("|                                 (vvv(VVV)(VVV)vvv)                     |");
        System.out.println("|                                  \\^^^^^^^^^^^^^^/                      |");
        System.out.println("|      ___.....____......_____......_'-.________.-'_......_____.....___  |");
        System.out.println("|      .-'\"\"\"\"\"'-..-'\"\"\"\"\"'-..-'\"\"\"\"'-..-'''''-..-'\"\"\"\"\"'-..-'\"\"\"\"\"'-..-  |");
        System.out.println("|      .-'\"\"\"\"\"'-..-'\"\"\"\"\"'-..-'\"\"\"\"'-..-'''''-..-'\"\"\"\"\"'-..-'\"\"\"\"\"'-..-  |");
        System.out.println("|                                                                        |");
        System.out.println("|          MATH DRAGON  |  HP: 80  |  DMG: 12  |  SPECIAL: INFERNO       |");
        System.out.println("|                                                                        |");
        System.out.println("+--------------------------------------------------------------------------+" + RESET);
    }

    // Prints the Victory screen inside a unified GREEN container box.
    public static void printVictory() {
        System.out.println(GREEN);
        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("|               *         .        +      *       +       *              |");
        System.out.println("|                        .------------------------.                      |");
        System.out.println("|            _.._        |   *   VICTORY!   *     |        _.._          |");
        System.out.println("|          .' .. '.______|                        |______.' .. '.        |");
        System.out.println("|          | |  `'                                        '`  | |        |");
        System.out.println("|          | |       (~)     *  CHAMPION  *     (~)           | |        |");
        System.out.println("|          | |        |\\                        /|            | |        |");
        System.out.println("|           \\ \\       | '._                  _.' |           / /         |");
        System.out.println("|            '. '._   '.   `'--..______..--'`   .'       _.' .'          |");
        System.out.println("|              `'-.`.   '.                    .'     _.-'`               |");
        System.out.println("|                  `'     \\                  /     '`                    |");
        System.out.println("|                          '.              .'                            |");
        System.out.println("|                            \\            /                              |");
        System.out.println("|                             '.   /\\   .'                               |");
        System.out.println("|                               |  ||  |                                 |");
        System.out.println("|                             .-'  ||  '-.                               |");
        System.out.println("|                            /     ||     \\                              |");
        System.out.println("|                        .-'`      ''      `'-.                          |");
        System.out.println("|                       |______________________|                         |");
        System.out.println("|                      |________________________|                        |");
        System.out.println("|                                                                        |");
        System.out.println("|                    YOU DEFEATED ALL THREE MONSTERS!                    |");
        System.out.println("|                                                                        |");
        System.out.println("+--------------------------------------------------------------------------+" + RESET);
    }

    // Prints the Game Over screen inside a unified RED container box.
    public static void printGameOver() {
        System.out.println(RED);
        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("|                          ...----''''''''----...                        |");
        System.out.println("|                       .-'                      '-.                     |");
        System.out.println("|                     .'                            '.                   |");
        System.out.println("|                   .'   __...--------....___         '.                 |");
        System.out.println("|                  /  .-'                    '-.        \\                |");
        System.out.println("|                 / .'                          '.       \\               |");
        System.out.println("|                | /      .----.      .----.      \\       |              |");
        System.out.println("|                ||      /      \\    /      \\      |      |              |");
        System.out.println("|                ||     |   ()   |  |   ()   |     |      |              |");
        System.out.println("|                ||      \\      /    \\      /      |      |              |");
        System.out.println("|                | \\      '----'  /\\  '----'      /       |              |");
        System.out.println("|                 \\ '.           /  \\           .'       /               |");
        System.out.println("|                  \\  '-.       '----'       .-'        /                |");
        System.out.println("|                   '.   '-..____________..-'         .'                 |");
        System.out.println("|                     '-.     |__|__|__|__|       _.-'                   |");
        System.out.println("|                        \\    |  |  |  |  |      /                       |");
        System.out.println("|                         '--.|__|__|__|__|.--''                         |");
        System.out.println("|                            / /|  |  |  |\\ \\                            |");
        System.out.println("|                           '-' '--'--'--' '-'                           |");
        System.out.println("|                                                                        |");
        System.out.println("|                     G  A  M  E      O  V  E  R                         |");
        System.out.println("|                                                                        |");
        System.out.println("+--------------------------------------------------------------------------+" + RESET);
    }
}