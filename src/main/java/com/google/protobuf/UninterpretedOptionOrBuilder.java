// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: descriptor.proto

package com.google.protobuf;

public interface UninterpretedOptionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.UninterpretedOption)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .google.protobuf.UninterpretedOption.NamePart name = 2;</code>
   */
  java.util.List<com.google.protobuf.UninterpretedOption.NamePart> 
      getNameList();
  /**
   * <code>repeated .google.protobuf.UninterpretedOption.NamePart name = 2;</code>
   */
  com.google.protobuf.UninterpretedOption.NamePart getName(int index);
  /**
   * <code>repeated .google.protobuf.UninterpretedOption.NamePart name = 2;</code>
   */
  int getNameCount();
  /**
   * <code>repeated .google.protobuf.UninterpretedOption.NamePart name = 2;</code>
   */
  java.util.List<? extends com.google.protobuf.UninterpretedOption.NamePartOrBuilder> 
      getNameOrBuilderList();
  /**
   * <code>repeated .google.protobuf.UninterpretedOption.NamePart name = 2;</code>
   */
  com.google.protobuf.UninterpretedOption.NamePartOrBuilder getNameOrBuilder(
      int index);

  /**
   * <code>optional string identifier_value = 3;</code>
   *
   * <pre>
   * The value of the uninterpreted option, in whatever type the tokenizer
   * identified it as during parsing. Exactly one of these should be set.
   * </pre>
   */
  boolean hasIdentifierValue();
  /**
   * <code>optional string identifier_value = 3;</code>
   *
   * <pre>
   * The value of the uninterpreted option, in whatever type the tokenizer
   * identified it as during parsing. Exactly one of these should be set.
   * </pre>
   */
  java.lang.String getIdentifierValue();
  /**
   * <code>optional string identifier_value = 3;</code>
   *
   * <pre>
   * The value of the uninterpreted option, in whatever type the tokenizer
   * identified it as during parsing. Exactly one of these should be set.
   * </pre>
   */
  com.google.protobuf.ByteString
      getIdentifierValueBytes();

  /**
   * <code>optional uint64 positive_int_value = 4;</code>
   */
  boolean hasPositiveIntValue();
  /**
   * <code>optional uint64 positive_int_value = 4;</code>
   */
  long getPositiveIntValue();

  /**
   * <code>optional int64 negative_int_value = 5;</code>
   */
  boolean hasNegativeIntValue();
  /**
   * <code>optional int64 negative_int_value = 5;</code>
   */
  long getNegativeIntValue();

  /**
   * <code>optional double double_value = 6;</code>
   */
  boolean hasDoubleValue();
  /**
   * <code>optional double double_value = 6;</code>
   */
  double getDoubleValue();

  /**
   * <code>optional bytes string_value = 7;</code>
   */
  boolean hasStringValue();
  /**
   * <code>optional bytes string_value = 7;</code>
   */
  com.google.protobuf.ByteString getStringValue();

  /**
   * <code>optional string aggregate_value = 8;</code>
   */
  boolean hasAggregateValue();
  /**
   * <code>optional string aggregate_value = 8;</code>
   */
  java.lang.String getAggregateValue();
  /**
   * <code>optional string aggregate_value = 8;</code>
   */
  com.google.protobuf.ByteString
      getAggregateValueBytes();
}
