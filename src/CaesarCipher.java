import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CaesarCipher {

    // Алфавитные константы
    private static final String ENGLISH_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ENGLISH_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String RUSSIAN_UPPER = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private static final String RUSSIAN_LOWER = "абвгдежзийклмнопрстуфхцчшщъыьэюя";

    // Шифрование текста
    public static String encrypt(String text, int key) {
        return process(text, key);
    }

    // Расшифровка текста с известным ключом
    public static String decrypt(String text, int key) {
        return process(text, -key);
    }

    // Общий метод обработки текста
    private static String process(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            String alphabetUpper;
            String alphabetLower;
            boolean isEnglish = false;
            boolean isRussian = false;

            // Проверка на английские буквы (оба регистра)
            if (ENGLISH_UPPER.indexOf(Character.toUpperCase(c)) != -1) {
                alphabetUpper = ENGLISH_UPPER;
                alphabetLower = ENGLISH_LOWER;
                isEnglish = true;
            }
            // Проверка на русские буквы (оба регистра)
            else if (RUSSIAN_UPPER.indexOf(Character.toUpperCase(c)) != -1) {
                alphabetUpper = RUSSIAN_UPPER;
                alphabetLower = RUSSIAN_LOWER;
                isRussian = true;
            } else {
                result.append(c);
                continue;
            }

            // Определение регистра символа
            boolean isUpper = Character.isUpperCase(c);
            String alphabet = isUpper ? alphabetUpper : alphabetLower;

            // Корректная обработка символа
            int index = alphabet.indexOf(c);
            if (index == -1) {
                result.append(c);
                continue;
            }

            int newIndex = (index + shift) % alphabet.length();
            if (newIndex < 0) newIndex += alphabet.length();
            result.append(alphabet.charAt(newIndex));
        }
        return result.toString();
    }


    // Brute force расшифровка
    public static List<String> bruteForceDecrypt(String encryptedText) {
        List<String> candidates = new ArrayList<>();
        int maxAlphabetSize = Math.max(ENGLISH_UPPER.length(), RUSSIAN_UPPER.length());
        for (int key = 1; key < maxAlphabetSize; key++) {
            candidates.add(decrypt(encryptedText, key));
        }
        return candidates;
    }

    // Статистический анализ
    public static int statisticalAnalysis(String encryptedText, Map<Character, Double> frequencyMap) {
        int bestKey = 0;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int key = 0; key < 33; key++) {
            String decrypted = decrypt(encryptedText, key);
            Map<Character, Integer> counts = new HashMap<>();
            int total = 0;

            for (char c : decrypted.toCharArray()) {
                if (Character.isLetter(c)) {
                    char lowerC = Character.toLowerCase(c);
                    counts.put(lowerC, counts.getOrDefault(lowerC, 0) + 1);
                    total++;
                }
            }

            double score = 0.0;
            for (Map.Entry<Character, Double> entry : frequencyMap.entrySet()) {
                char c = entry.getKey();
                double expected = entry.getValue();
                int observed = counts.getOrDefault(c, 0);
                score += Math.log((observed + 1e-10) / (total + 1e-10 * frequencyMap.size())) * expected;
            }

            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }
        return bestKey;
    }

    // Работа с файлами
    public static void processFile(String inputFile, String outputFile, int key, boolean encryptMode) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String processed = encryptMode ? encrypt(line, key) : decrypt(line, key);
                writer.write(processed);
                writer.newLine();
            }
        }
    }

    // Текстовый интерфейс
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("Шифр Цезаря");

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Шифровать файл");
            System.out.println("2. Расшифровать файл с ключом");
            System.out.println("3. Brute force расшифровка");
            System.out.println("4. Статистический анализ");
            System.out.println("5. Выход");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число от 1 до 5");
                continue;
            }

            switch (choice) {
                case 1:
                case 2:
                    try {
                        System.out.println("Введите входной файл:");
                        String input = scanner.nextLine();
                        System.out.println("Введите выходной файл:");
                        String output = scanner.nextLine();
                        System.out.println("Введите ключ:");
                        int key = Integer.parseInt(scanner.nextLine());
                        processFile(input, output, key, choice == 1);
                        System.out.println("Операция завершена успешно");
                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        System.out.println("Введите входной файл:");
                        String input = scanner.nextLine();
                        System.out.println("Введите выходной файл для результатов:");
                        String output = scanner.nextLine();

                        StringBuilder encryptedText = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new FileReader(input, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                encryptedText.append(line).append("\n");
                            }
                        }

                        List<String> candidates = bruteForceDecrypt(encryptedText.toString());
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output, StandardCharsets.UTF_8))) {
                            for (int i = 0; i < candidates.size(); i++) {
                                writer.write("Ключ " + (i + 1) + ":\n");
                                writer.write(candidates.get(i));
                                writer.write("\n\n");
                            }
                        }
                        System.out.println("Brute force завершен. Проверьте выходной файл.");
                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                    break;

                case 4:
                    try {
                        // Пример частот для русского языка (упрощенно)
                        Map<Character, Double> russianFreq = new HashMap<>();
                        russianFreq.put('о', 0.09);
                        russianFreq.put('е', 0.08);
                        russianFreq.put('а', 0.07);
                        // Добавьте другие частоты...

                        System.out.println("Введите входной файл:");
                        String input = scanner.nextLine();

                        StringBuilder encryptedText = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new FileReader(input, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                encryptedText.append(line);
                            }
                        }

                        int key = statisticalAnalysis(encryptedText.toString(), russianFreq);
                        System.out.println("Предполагаемый ключ: " + key);
                        System.out.println("Расшифрованный текст:\n" + decrypt(encryptedText.toString(), key));
                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("Выход...");
                    return;

                default:
                    System.out.println("Неверный выбор");
            }
        }
    }
}
