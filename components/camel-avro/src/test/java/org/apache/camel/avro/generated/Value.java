/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.apache.camel.avro.generated;  
@SuppressWarnings("all")
public class Value extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"Value\",\"namespace\":\"org.apache.camel.avro.generated\",\"fields\":[{\"name\":\"value\",\"type\":\"string\"}]}");
  @Deprecated public java.lang.CharSequence value;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return value;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: value = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'value' field.
   */
  public java.lang.CharSequence getValue() {
    return value;
  }

  /**
   * Sets the value of the 'value' field.
   * @param value the value to set.
   */
  public void setValue(java.lang.CharSequence value) {
    this.value = value;
  }

  /** Creates a new Value RecordBuilder */
  public static org.apache.camel.avro.generated.Value.Builder newBuilder() {
    return new org.apache.camel.avro.generated.Value.Builder();
  }
  
  /** Creates a new Value RecordBuilder by copying an existing Builder */
  public static org.apache.camel.avro.generated.Value.Builder newBuilder(org.apache.camel.avro.generated.Value.Builder other) {
    return new org.apache.camel.avro.generated.Value.Builder(other);
  }
  
  /** Creates a new Value RecordBuilder by copying an existing Value instance */
  public static org.apache.camel.avro.generated.Value.Builder newBuilder(org.apache.camel.avro.generated.Value other) {
    return new org.apache.camel.avro.generated.Value.Builder(other);
  }
  
  /**
   * RecordBuilder for Value instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Value>
    implements org.apache.avro.data.RecordBuilder<Value> {

    private java.lang.CharSequence value;

    /** Creates a new Builder */
    private Builder() {
      super(org.apache.camel.avro.generated.Value.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.apache.camel.avro.generated.Value.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing Value instance */
    private Builder(org.apache.camel.avro.generated.Value other) {
            super(org.apache.camel.avro.generated.Value.SCHEMA$);
      if (isValidValue(fields()[0], other.value)) {
        this.value = (java.lang.CharSequence) data().deepCopy(fields()[0].schema(), other.value);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'value' field */
    public java.lang.CharSequence getValue() {
      return value;
    }
    
    /** Sets the value of the 'value' field */
    public org.apache.camel.avro.generated.Value.Builder setValue(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.value = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'value' field has been set */
    public boolean hasValue() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'value' field */
    public org.apache.camel.avro.generated.Value.Builder clearValue() {
      value = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public Value build() {
      try {
        Value record = new Value();
        record.value = fieldSetFlags()[0] ? this.value : (java.lang.CharSequence) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
