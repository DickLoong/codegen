// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: descriptor.proto

package com.google.protobuf;

public interface EnumValueOptionsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.EnumValueOptions)
    com.google.protobuf.GeneratedMessage.
        ExtendableMessageOrBuilder<EnumValueOptions> {

  /**
   * <code>optional bool deprecated = 1 [default = false];</code>
   *
   * <pre>
   * Is this enum value deprecated?
   * Depending on the target platform, this can emit Deprecated annotations
   * for the enum value, or it will be completely ignored; in the very least,
   * this is a formalization for deprecating enum values.
   * </pre>
   */
  boolean hasDeprecated();
  /**
   * <code>optional bool deprecated = 1 [default = false];</code>
   *
   * <pre>
   * Is this enum value deprecated?
   * Depending on the target platform, this can emit Deprecated annotations
   * for the enum value, or it will be completely ignored; in the very least,
   * this is a formalization for deprecating enum values.
   * </pre>
   */
  boolean getDeprecated();

  /**
   * <code>repeated .google.protobuf.UninterpretedOption uninterpreted_option = 999;</code>
   *
   * <pre>
   * The parser stores options it doesn't recognize here. See above.
   * </pre>
   */
  java.util.List<com.google.protobuf.UninterpretedOption> 
      getUninterpretedOptionList();
  /**
   * <code>repeated .google.protobuf.UninterpretedOption uninterpreted_option = 999;</code>
   *
   * <pre>
   * The parser stores options it doesn't recognize here. See above.
   * </pre>
   */
  com.google.protobuf.UninterpretedOption getUninterpretedOption(int index);
  /**
   * <code>repeated .google.protobuf.UninterpretedOption uninterpreted_option = 999;</code>
   *
   * <pre>
   * The parser stores options it doesn't recognize here. See above.
   * </pre>
   */
  int getUninterpretedOptionCount();
  /**
   * <code>repeated .google.protobuf.UninterpretedOption uninterpreted_option = 999;</code>
   *
   * <pre>
   * The parser stores options it doesn't recognize here. See above.
   * </pre>
   */
  java.util.List<? extends com.google.protobuf.UninterpretedOptionOrBuilder> 
      getUninterpretedOptionOrBuilderList();
  /**
   * <code>repeated .google.protobuf.UninterpretedOption uninterpreted_option = 999;</code>
   *
   * <pre>
   * The parser stores options it doesn't recognize here. See above.
   * </pre>
   */
  com.google.protobuf.UninterpretedOptionOrBuilder getUninterpretedOptionOrBuilder(
      int index);
}