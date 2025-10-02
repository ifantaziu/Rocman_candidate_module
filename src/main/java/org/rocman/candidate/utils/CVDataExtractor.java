//package org.rocman.candidate.utils;
//
//import lombok.extern.log4j.Log4j2;
//import org.rocman.candidate.dtos.*;
//
//import java.util.*;
//import java.util.regex.*;
//
//@Log4j2
//public class CVDataExtractor {
//
//    public static CandidateProfileDTO extractFields(String text) {
//        CandidateProfileDTO data = new CandidateProfileDTO();
//        String normalizedText = text.replaceAll("\\s+", " ").trim();
//
//        log.info("Starting structured CV extraction...");
//
//        data.setEmail(extractPattern(normalizedText, "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}"));
//        data.setPhone(extractPattern(normalizedText, "\\+?\\d{7,15}"));
//
//        String fullName = extractFullName(normalizedText);
//        if (fullName != null) {
//            String[] parts = fullName.split("\\s+");
//            if (parts.length >= 2) {
//                data.setFirstName(parts[0]);
//                data.setLastName(parts[1]);
//            }
//        }
//
//        data.setAddress(extractAddress(normalizedText));
//
//        data.setEducation(extractEducation(normalizedText));
//        data.setExperience(extractExperience(normalizedText));
//        data.setSkills(extractSkills(normalizedText));
//        data.setLanguages(extractLanguages(normalizedText));
//
//        log.info("Extraction finished. Candidate detected: {} {}", data.getFirstName(), data.getLastName());
//        return data;
//    }
//
//    private static String extractPattern(String text, String regex) {
//        Matcher matcher = Pattern.compile(regex).matcher(text);
//        if (matcher.find()) {
//            log.debug("Pattern [{}] matched: {}", regex, matcher.group());
//            return matcher.group();
//        }
//        return null;
//    }
//
//    private static String extractFullName(String text) {
//        Matcher matcher = Pattern.compile("\\b([A-Z][a-z]+|[А-ЯЁ][а-яё]+)\\s+([A-Z][a-z]+|[А-ЯЁ][а-яё]+)").matcher(text);
//        return matcher.find() ? matcher.group() : null;
//    }
//
//    public static String extractAddress(String text) {
//        Matcher matcher = Pattern.compile("(?i)(address|adresse |str |strada|street|адрес)[:]? ([^,\\n]+)").matcher(text);
//        return matcher.find() ? matcher.group(2).trim() : null;
//    }
//
//    private static List<CandidateProfileDTO.EducationDTO> extractEducation(String text) {
//        List<CandidateProfileDTO.EducationDTO> educationList = new ArrayList<>();
//        Pattern pattern = Pattern.compile(
//                "(?i)(education|training|educație|bachelor|ba|ma|master|doctor|phd|licență|facultate|universitate|colegiu|curs|certificare|licence|" +
//                        "licence professionnelle|doctorat|cours|formation|certification|diplôme|bac+3|bac+5|éducation|университет|бакалавр|магистр)" +
//                        "[^,\\n]*,\\s*([\\p{L} .]+),\\s*([0-9]{4}(?:-[0-9]{4}| – prezent| – насто(?:я|и)щее время))"
//        );
//        Matcher matcher = pattern.matcher(text);
//
//        while (matcher.find()) {
//            CandidateProfileDTO.EducationDTO edu = new CandidateProfileDTO.EducationDTO();
//            edu.setLevel(matcher.group(1).trim());
//            edu.setInstitution(matcher.group(2).trim());
//            edu.setPeriod(matcher.group(3).trim());
//            educationList.add(edu);
//        }
//
//        return educationList;
//    }
//
//    private static List<CandidateProfileDTO.ExperienceDTO> extractExperience(String text) {
//        List<CandidateProfileDTO.ExperienceDTO> experiences = new ArrayList<>();
//
//        Pattern pattern = Pattern.compile(
//                "(?i)([\\p{L} ]+?)\\s*,\\s*([\\p{L} .]+?)\\s*,\\s*([0-9]{4}(?:-[0-9]{4}| – [0-9]{4}|present| présent| – prezent| – насто(?:я|и)щее время))"
//        );
//        Matcher matcher = pattern.matcher(text);
//
//        while (matcher.find()) {
//            CandidateProfileDTO.ExperienceDTO exp = new CandidateProfileDTO.ExperienceDTO();
//            exp.setTitle(matcher.group(1).trim());
//            exp.setCompany(matcher.group(2).trim());
//            exp.setPeriod(matcher.group(3).trim());
//            experiences.add(exp);
//        }
//
//        return experiences;
//    }
//
//
//    private static List<CandidateProfileDTO.SkillDTO> extractSkills(String text) {
//        List<CandidateProfileDTO.SkillDTO> list = new ArrayList<>();
//        Matcher matcher = Pattern.compile("(?i)(skills|competencies|calificari|competențe|compétences |навыки)[:]?([^\\n]+)").matcher(text);
//        if (matcher.find()) {
//            String[] tokens = matcher.group(2).split("[,;•]");
//            for (String token : tokens) {
//                CandidateProfileDTO.SkillDTO dto = new CandidateProfileDTO.SkillDTO();
//                dto.setName(token.trim());
//                list.add(dto);
//            }
//        }
//        return list;
//    }
//
//    private static List<CandidateProfileDTO.LanguageDTO> extractLanguages(String text) {
//        List<CandidateProfileDTO.LanguageDTO> languages = new ArrayList<>();
//
//        Pattern sectionPattern = Pattern.compile("(?i)(languages|limbi străine|langues|yaz[y|i]ki|языки)[:]?([^\\n]+)");
//        Matcher sectionMatcher = sectionPattern.matcher(text);
//
//        if (sectionMatcher.find()) {
//            String langsRaw = sectionMatcher.group(2);
//            String[] entries = langsRaw.split("[,;]");
//
//            for (String entry : entries) {
//                String lang = entry.trim();
//
//                Matcher levelMatcher = Pattern.compile(
//                        "(A1|A2|B1|B2|C1|C2|toefl|ielts|cambridge|delf|dalf|goethe|native|materna|nativ|natif|maternelle|bilingue|maitrise|fluent|autonome|материнский|носитель|advanced|avansat|avance|продвинутый|independent|proficient|conversational|seuil|intermediate|intermediar|intermediaire|средний|beginner|начальный|elementar|baza|elementary|элементарный|basic|incepator|debutant)",
//                        Pattern.CASE_INSENSITIVE
//                ).matcher(lang);
//
//                String level = null;
//                if (levelMatcher.find()) {
//                    level = levelMatcher.group().toUpperCase();
//                }
//
//                String languageName = lang.replaceAll(
//                        "(?i)(A1|A2|B1|B2|C1|C2|toefl|ielts|cambridge|delf|dalf|goethe|native|materna|nativ|natif|maternelle|bilingue|maitrise|fluent|autonome|материнский|носитель|advanced|avansat|avance|продвинутый|independent|proficient|conversational|seuil|intermediate|intermediar|intermediaire|средний|beginner|начальный|elementar|baza|elementary|элементарный|basic|incepator|debutant)",
//                        ""
//                ).trim();
//
//                CandidateProfileDTO.LanguageDTO dto = new CandidateProfileDTO.LanguageDTO();
//                dto.setLanguage(languageName);
//                dto.setLevel(level != null ? level : "UNKNOWN");
//                languages.add(dto);
//            }
//        }
//
//        return languages;
//    }
//}
