import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> documents;

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        // In the real project maybe safer to create DocumentDto and receive author as an id
        if (isValidDocument(document)) {
            if (isNullOrEmpty(document.getId())) {
                document.setId(UUID.randomUUID().toString());
            }
            documents.add(document);
        }
        return null;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documents.stream()
                .filter(doc -> matchesAuthor(doc, request.getAuthorIds()))
                .filter(doc -> matchesCreatedRange(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .filter(doc -> matchesTitlePrefix(doc, request.getTitlePrefixes()))
                .filter(doc -> matchesContent(doc, request.getContainsContents()))
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return documents.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    private boolean isValidDocument(Document document) {
        //In case of backend app I would prefer to throw some exceptions than catch them in the handler
        if (document != null) {
            return !isNullOrEmpty(document.getTitle())
                    && !isNullOrEmpty(document.getContent())
                    && document.getAuthor() != null
                    && !isNullOrEmpty(document.getAuthor().getId())
                    && document.getCreated() != null
                    && document.getCreated().isAfter(Instant.now());
        }
        return false;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    private boolean matchesTitlePrefix(Document doc, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            return true;
        }
        return prefixes.stream()
                .anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    private boolean matchesContent(Document doc, List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return true;
        }
        return contents.stream()
                .anyMatch(content -> doc.getContent().contains(content));
    }

    private boolean matchesAuthor(Document doc, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        return authorIds.contains(doc.getAuthor().getId());
    }

    private boolean matchesCreatedRange(Document doc, Instant from, Instant to) {
        Instant created = doc.getCreated();
        if (from != null && created.isBefore(from)) {
            return false;
        }
        if (to != null && created.isAfter(to)) {
            return false;
        }
        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}