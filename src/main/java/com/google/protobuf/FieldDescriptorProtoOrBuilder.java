// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: descriptor.proto

package com.google.protobuf;

public interface FieldDescriptorProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.FieldDescriptorProto)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   */
  boolean hasName();
  /**
   * <code>optional string name = 1;</code>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional int32 number = 3;</code>
   */
  boolean hasNumber();
  /**
   * <code>optional int32 number = 3;</code>
   */
  int getNumber();

  /**
   * <code>optional .google.protobuf.FieldDescriptorProto.Label label = 4;</code>
   */
  boolean hasLabel();
  /**
   * <code>optional .google.protobuf.FieldDescriptorProto.Label label = 4;</code>
   */
  com.google.protobuf.FieldDescriptorProto.Label getLabel();

  /**
   * <code>optional .google.protobuf.FieldDescriptorProto.Type type = 5;</code>
   *
   * <pre>
   * If type_name is set, this need not be set.  If both this and type_name
   * are set, this must be one of TYPE_ENUM, TYPE_MESSAGE or TYPE_GROUP.
   * </pre>
   */
  boolean hasType();
  /**
   * <code>optional .google.protobuf.FieldDescriptorProto.Type type = 5;</code>
   *
   * <pre>
   * If type_name is set, this need not be set.  If both this and type_name
   * are set, this must be one of TYPE_ENUM, TYPE_MESSAGE or TYPE_GROUP.
   * </pre>
   */
  com.google.protobuf.FieldDescriptorProto.Type getType();

  /**
   * <code>optional string type_name = 6;</code>
   *
   * <pre>
   * For message and enum types, this is the name of the type.  If the name
   * starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping
   * rules are used to find the type (i.e. first the nested types within this
   * message are searched, then within the parent, on up to the root
   * namespace).
   * </pre>
   */
  boolean hasTypeName();
  /**
   * <code>optional string type_name = 6;</code>
   *
   * <pre>
   * For message and enum types, this is the name of the type.  If the name
   * starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping
   * rules are used to find the type (i.e. first the nested types within this
   * message are searched, then within the parent, on up to the root
   * namespace).
   * </pre>
   */
  java.lang.String getTypeName();
  /**
   * <code>optional string type_name = 6;</code>
   *
   * <pre>
   * For message and enum types, this is the name of the type.  If the name
   * starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping
   * rules are used to find the type (i.e. first the nested types within this
   * message are searched, then within the parent, on up to the root
   * namespace).
   * </pre>
   */
  com.google.protobuf.ByteString
      getTypeNameBytes();

  /**
   * <code>optional string extendee = 2;</code>
   *
   * <pre>
   * For extensions, this is the name of the type being extended.  It is
   * resolved in the same manner as type_name.
   * </pre>
   */
  boolean hasExtendee();
  /**
   * <code>optional string extendee = 2;</code>
   *
   * <pre>
   * For extensions, this is the name of the type being extended.  It is
   * resolved in the same manner as type_name.
   * </pre>
   */
  java.lang.String getExtendee();
  /**
   * <code>optional string extendee = 2;</code>
   *
   * <pre>
   * For extensions, this is the name of the type being extended.  It is
   * resolved in the same manner as type_name.
   * </pre>
   */
  com.google.protobuf.ByteString
      getExtendeeBytes();

  /**
   * <code>optional string default_value = 7;</code>
   *
   * <pre>
   * For numeric types, contains the original text representation of the value.
   * For booleans, "true" or "false".
   * For strings, contains the default text contents (not escaped in any way).
   * For bytes, contains the C escaped value.  All bytes &gt;= 128 are escaped.
   * TODO(kenton):  Base-64 encode?
   * </pre>
   */
  boolean hasDefaultValue();
  /**
   * <code>optional string default_value = 7;</code>
   *
   * <pre>
   * For numeric types, contains the original text representation of the value.
   * For booleans, "true" or "false".
   * For strings, contains the default text contents (not escaped in any way).
   * For bytes, contains the C escaped value.  All bytes &gt;= 128 are escaped.
   * TODO(kenton):  Base-64 encode?
   * </pre>
   */
  java.lang.String getDefaultValue();
  /**
   * <code>optional string default_value = 7;</code>
   *
   * <pre>
   * For numeric types, contains the original text representation of the value.
   * For booleans, "true" or "false".
   * For strings, contains the default text contents (not escaped in any way).
   * For bytes, contains the C escaped value.  All bytes &gt;= 128 are escaped.
   * TODO(kenton):  Base-64 encode?
   * </pre>
   */
  com.google.protobuf.ByteString
      getDefaultValueBytes();

  /**
   * <code>optional int32 oneof_index = 9;</code>
   *
   * <pre>
   * If set, gives the index of a oneof in the containing type's oneof_decl
   * list.  This field is a member of that oneof.  Extensions of a oneof should
   * not set this since the oneof to which they belong will be inferred based
   * on the extension range containing the extension's field number.
   * </pre>
   */
  boolean hasOneofIndex();
  /**
   * <code>optional int32 oneof_index = 9;</code>
   *
   * <pre>
   * If set, gives the index of a oneof in the containing type's oneof_decl
   * list.  This field is a member of that oneof.  Extensions of a oneof should
   * not set this since the oneof to which they belong will be inferred based
   * on the extension range containing the extension's field number.
   * </pre>
   */
  int getOneofIndex();

  /**
   * <code>optional .google.protobuf.FieldOptions options = 8;</code>
   */
  boolean hasOptions();
  /**
   * <code>optional .google.protobuf.FieldOptions options = 8;</code>
   */
  com.google.protobuf.FieldOptions getOptions();
  /**
   * <code>optional .google.protobuf.FieldOptions options = 8;</code>
   */
  com.google.protobuf.FieldOptionsOrBuilder getOptionsOrBuilder();
}