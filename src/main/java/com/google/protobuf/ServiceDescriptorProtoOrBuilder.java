// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: descriptor.proto

package com.google.protobuf;

public interface ServiceDescriptorProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.ServiceDescriptorProto)
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
   * <code>repeated .google.protobuf.MethodDescriptorProto method = 2;</code>
   */
  java.util.List<com.google.protobuf.MethodDescriptorProto> 
      getMethodList();
  /**
   * <code>repeated .google.protobuf.MethodDescriptorProto method = 2;</code>
   */
  com.google.protobuf.MethodDescriptorProto getMethod(int index);
  /**
   * <code>repeated .google.protobuf.MethodDescriptorProto method = 2;</code>
   */
  int getMethodCount();
  /**
   * <code>repeated .google.protobuf.MethodDescriptorProto method = 2;</code>
   */
  java.util.List<? extends com.google.protobuf.MethodDescriptorProtoOrBuilder> 
      getMethodOrBuilderList();
  /**
   * <code>repeated .google.protobuf.MethodDescriptorProto method = 2;</code>
   */
  com.google.protobuf.MethodDescriptorProtoOrBuilder getMethodOrBuilder(
      int index);

  /**
   * <code>optional .google.protobuf.ServiceOptions options = 3;</code>
   */
  boolean hasOptions();
  /**
   * <code>optional .google.protobuf.ServiceOptions options = 3;</code>
   */
  com.google.protobuf.ServiceOptions getOptions();
  /**
   * <code>optional .google.protobuf.ServiceOptions options = 3;</code>
   */
  com.google.protobuf.ServiceOptionsOrBuilder getOptionsOrBuilder();
}
