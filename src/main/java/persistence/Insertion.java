package persistence;

import java.util.List;

public final class Insertion {
  private final List<String> columns;
  private final List<String> fields;

  public Insertion(List<String> columns, List<String> fields) {
    this.columns = columns;
    this.fields = fields;
  }

  public List<String> getColumns() {
    return columns;
  }

  public List<String> getFields() {
    return fields;
  }
}
