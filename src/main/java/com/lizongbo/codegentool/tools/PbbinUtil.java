package com.lizongbo.codegentool.tools;

import com.google.protobuf.DescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.EnumDescriptorProto;
import com.google.protobuf.FieldDescriptorProto.*;
import com.google.protobuf.*;
import java.util.*;

import java.io.*;

public class PbbinUtil {
	private static Map<String, List<String>> enumClassMap = new TreeMap<String, List<String>>();

	public static void main(String[] args) {
		String pbbinFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/protofilestmp/TProto_Cmd.pbbin";
		pbbinFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/protofilestmp/PVEreportResultPKProtoBufResponse.pbbin";
		pbbin2SerializerCs(pbbinFilePath, "");
	}

	public static void pbbin2SerializerCs(String pbFilePath, String csFilePath) {
		byte[] bs = readFileBytes(pbFilePath);
		try {
			FileDescriptorSet fds = FileDescriptorSet.parseFrom(bs);
			for (FileDescriptorProto fd : fds.getFileList()) {
				System.out.println("fd.name=" + fd.getName());
				System.out.println("fd.getPackage=" + fd.getPackage());
				System.out.println("fd.getJavaPackage=" + fd.getOptions().getJavaPackage());
				for (EnumDescriptorProto msgType : fd.getEnumTypeList()) {
					List<String> vList = new ArrayList<String>();
					for (EnumValueDescriptorProto evdp : msgType.getValueList()) {
						vList.add(evdp.getName());
					}
					enumClassMap.put("." + fd.getPackage() + "." + msgType.getName(), vList);
				}
				System.out.println("enumClassMap==" + enumClassMap);
				for (DescriptorProto msgType : fd.getMessageTypeList()) {
					System.out.println(msgType.getName() + "=================");
					for (FieldDescriptorProto fdp : msgType.getFieldList()) {
						System.out.println(fdp.getType() + "|" + fdp.getTypeName() + " " + fdp.getName() + " = "
								+ fdp.getDefaultValue());
					}
					StringBuilder sb = new StringBuilder(1024 * 64);
					appendSb(sb, msgType, fd, fds);
					System.out.println(sb);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拼字符串生成cs文件
	 * 
	 * @param sb
	 * @param dp
	 * @return
	 */
	public static boolean appendSb(StringBuilder sb, DescriptorProto dp, FileDescriptorProto fd,
			FileDescriptorSet fds) {
		String className = dp.getName();
		sb.append(" [System.Serializable()]\n");
		sb.append("    public partial class " + className + "\n");
		sb.append("    {\n");
		sb.append("        /// <summary>Helper: create a new instance to deserializing into</summary>\n");
		sb.append("        public static " + className + " Deserialize(Stream stream)\n");
		sb.append("        {\n");
		sb.append("            " + className + " instance = new " + className + "();\n");
		sb.append("            Deserialize(stream, instance);\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append("        /// <summary>Helper: create a new instance to deserializing into</summary>\n");
		sb.append("        public static " + className + " DeserializeLengthDelimited(Stream stream)\n");
		sb.append("        {\n");
		sb.append("            " + className + " instance = new " + className + "();\n");
		sb.append("            DeserializeLengthDelimited(stream, instance);\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append("        /// <summary>Helper: create a new instance to deserializing into</summary>\n");
		sb.append("        public static " + className + " DeserializeLength(Stream stream, int length)\n");
		sb.append("        {\n");
		sb.append("            " + className + " instance = new " + className + "();\n");
		sb.append("            DeserializeLength(stream, length, instance);\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append(
				"        /// <summary>Helper: put the buffer into a MemoryStream and create a new instance to deserializing into</summary>\n");
		sb.append("        public static " + className + " Deserialize(byte[] buffer)\n");
		sb.append("        {\n");
		sb.append("            " + className + " instance = new " + className + "();\n");
		sb.append("            using (var ms = new MemoryStream(buffer))\n");
		sb.append("                Deserialize(ms, instance);\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append("        /// <summary>Helper: put the buffer into a MemoryStream before deserializing</summary>\n");
		sb.append("        public static " + fd.getPackage() + "." + className + " Deserialize(byte[] buffer, "
				+ fd.getPackage() + "." + className + " instance)\n");
		sb.append("        {\n");
		sb.append("            using (var ms = new MemoryStream(buffer))\n");
		sb.append("                Deserialize(ms, instance);\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append(
				"        /// <summary>Takes the remaining content of the stream and deserialze it into the instance.</summary>\n");
		sb.append("        public static " + fd.getPackage() + "." + className + " Deserialize(Stream stream, "
				+ fd.getPackage() + "." + className + " instance)\n");
		sb.append("        {\n");
		for (FieldDescriptorProto ffd : dp.getFieldList()) {
			// 枚举的需要在这里定义一下
			if (ffd.getType().equals(com.google.protobuf.FieldDescriptorProto.Type.TYPE_ENUM)) {
				if (ffd.hasDefaultValue()) {
					System.err.println("枚举有默认值：" + ffd);
					sb.append("            instance.dataNotifyType = " + ffd.getDefaultValue() + ";\n");
				} else if (enumClassMap.get(ffd.getTypeName()) != null) {
					System.err.println("枚举强行指定第一个变量为默认值：" + ffd);
					sb.append("            instance.dataNotifyType = " + ffd.getTypeName().substring(1) + "."
							+ enumClassMap.get(ffd.getTypeName()).get(0) + ";\n");

				} else {
					sb.append("            instance.dataNotifyType = errr;\n");
				}
			}
		}
		sb.append("            while (true)\n");
		sb.append("            {\n");
		sb.append("                int keyByte = stream.ReadByte();\n");
		sb.append("                if (keyByte == -1)\n");
		sb.append("                    break;\n");
		sb.append("                // Optimized reading of known fields with field ID < 16\n");
		sb.append("                switch (keyByte)\n");
		sb.append("                {\n");

		for (FieldDescriptorProto ffd : dp.getFieldList()) {
			int tag = 0;
			if (ffd.getType().equals(FieldDescriptorProto.Type.TYPE_ENUM)) {// 枚举
				sb.append("                    // Field " + ffd.getNumber() + " " + getPbTypeStr(ffd.getType()) + "\n");
				sb.append("                    case " + getPbtag(ffd.getNumber(), ffd.getType()) + ":\n");
				sb.append("                        instance." + ffd.getName() + " = (" + ffd.getTypeName()
						+ ")global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadUInt64(stream);\n");
				sb.append("                        continue;\n");
			}
		}

		sb.append("                    // Field 2 LengthDelimited\n");
		sb.append("                    case 18:\n");
		sb.append(
				"                        instance.dataNotifyBytes = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadBytes(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                    // Field 3 LengthDelimited\n");
		sb.append("                    case 26:\n");
		sb.append(
				"                        instance.jsonDesc = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadString(stream);\n");
		sb.append("                        continue;\n");

		sb.append("                }\n");
		sb.append("\n");
		sb.append(
				"                var key = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadKey((byte)keyByte, stream);\n");
		sb.append("\n");
		sb.append("                // Reading field ID > 16 and unknown field ID/wire type combinations\n");
		sb.append("                switch (key.Field)\n");
		sb.append("                {\n");
		sb.append("                    case 0:\n");
		sb.append(
				"                        throw new global::SilentOrbit.ProtocolBuffers.ProtocolBufferException(\"Invalid field id: 0, something went wrong in the stream\");\n");
		sb.append("                    default:\n");
		sb.append("                        global::SilentOrbit.ProtocolBuffers.ProtocolParser.SkipKey(stream, key);\n");
		sb.append("                        break;\n");
		sb.append("                }\n");
		sb.append("            }\n");
		sb.append("\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append(
				"        /// <summary>Read the VarInt length prefix and the given number of bytes from the stream and deserialze it into the instance.</summary>\n");
		sb.append("        public static " + fd.getPackage() + "." + className
				+ " DeserializeLengthDelimited(Stream stream, " + fd.getPackage() + "." + className + " instance)\n");
		sb.append("        {\n");
		for (FieldDescriptorProto ffd : dp.getFieldList()) {
			// 枚举的需要在这里定义一下
			if (ffd.getType().equals(com.google.protobuf.FieldDescriptorProto.Type.TYPE_ENUM)) {
				if (ffd.hasDefaultValue()) {
					System.err.println("枚举有默认值：" + ffd);
					sb.append("            instance.dataNotifyType = " + ffd.getDefaultValue() + ";\n");
				} else if (enumClassMap.get(ffd.getTypeName()) != null) {
					System.err.println("枚举强行指定第一个变量为默认值：" + ffd);
					sb.append("            instance.dataNotifyType = " + ffd.getTypeName().substring(1) + "."
							+ enumClassMap.get(ffd.getTypeName()).get(0) + ";\n");

				} else {
					sb.append("            instance.dataNotifyType = errr;\n");
				}
			}
		}
		sb.append("            long limit = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadUInt32(stream);\n");
		sb.append("            limit += stream.Position;\n");
		sb.append("            while (true)\n");
		sb.append("            {\n");
		sb.append("                if (stream.Position >= limit)\n");
		sb.append("                {\n");
		sb.append("                    if (stream.Position == limit)\n");
		sb.append("                        break;\n");
		sb.append("                    else\n");
		sb.append(
				"                        throw new global::SilentOrbit.ProtocolBuffers.ProtocolBufferException(\"Read past max limit\");\n");
		sb.append("                }\n");
		sb.append("                int keyByte = stream.ReadByte();\n");
		sb.append("                if (keyByte == -1)\n");
		sb.append("                    throw new System.IO.EndOfStreamException();\n");
		sb.append("                // Optimized reading of known fields with field ID < 16\n");
		sb.append("                switch (keyByte)\n");
		sb.append("                {\n");
		sb.append("                    // Field 1 Varint\n");
		sb.append("                    case 8:\n");
		sb.append("                        instance.dataNotifyType = (" + fd.getPackage()
				+ ".DataNotifyType)global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadUInt64(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                    // Field 2 LengthDelimited\n");
		sb.append("                    case 18:\n");
		sb.append(
				"                        instance.dataNotifyBytes = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadBytes(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                    // Field 3 LengthDelimited\n");
		sb.append("                    case 26:\n");
		sb.append(
				"                        instance.jsonDesc = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadString(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                }\n");
		sb.append("\n");
		sb.append(
				"                var key = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadKey((byte)keyByte, stream);\n");
		sb.append("\n");
		sb.append("                // Reading field ID > 16 and unknown field ID/wire type combinations\n");
		sb.append("                switch (key.Field)\n");
		sb.append("                {\n");
		sb.append("                    case 0:\n");
		sb.append(
				"                        throw new global::SilentOrbit.ProtocolBuffers.ProtocolBufferException(\"Invalid field id: 0, something went wrong in the stream\");\n");
		sb.append("                    default:\n");
		sb.append("                        global::SilentOrbit.ProtocolBuffers.ProtocolParser.SkipKey(stream, key);\n");
		sb.append("                        break;\n");
		sb.append("                }\n");
		sb.append("            }\n");
		sb.append("\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append(
				"        /// <summary>Read the given number of bytes from the stream and deserialze it into the instance.</summary>\n");
		sb.append("        public static " + fd.getPackage() + "." + className
				+ " DeserializeLength(Stream stream, int length, " + fd.getPackage() + "." + className
				+ " instance)\n");
		sb.append("        {\n");
		for (FieldDescriptorProto ffd : dp.getFieldList()) {
			// 枚举的需要在这里定义一下
			if (ffd.getType().equals(com.google.protobuf.FieldDescriptorProto.Type.TYPE_ENUM)) {
				if (ffd.hasDefaultValue()) {
					System.err.println("枚举有默认值：" + ffd);
					sb.append("            instance.dataNotifyType = " + ffd.getDefaultValue() + ";\n");
				} else if (enumClassMap.get(ffd.getTypeName()) != null) {
					System.err.println("枚举强行指定第一个变量为默认值：" + ffd);
					sb.append("            instance.dataNotifyType = " + ffd.getTypeName().substring(1) + "."
							+ enumClassMap.get(ffd.getTypeName()).get(0) + ";\n");

				} else {
					sb.append("            instance.dataNotifyType = errr;\n");
				}
			}
		}
		sb.append("            long limit = stream.Position + length;\n");
		sb.append("            while (true)\n");
		sb.append("            {\n");
		sb.append("                if (stream.Position >= limit)\n");
		sb.append("                {\n");
		sb.append("                    if (stream.Position == limit)\n");
		sb.append("                        break;\n");
		sb.append("                    else\n");
		sb.append(
				"                        throw new global::SilentOrbit.ProtocolBuffers.ProtocolBufferException(\"Read past max limit\");\n");
		sb.append("                }\n");
		sb.append("                int keyByte = stream.ReadByte();\n");
		sb.append("                if (keyByte == -1)\n");
		sb.append("                    throw new System.IO.EndOfStreamException();\n");
		sb.append("                // Optimized reading of known fields with field ID < 16\n");
		sb.append("                switch (keyByte)\n");
		sb.append("                {\n");
		sb.append("                    // Field 1 Varint\n");
		sb.append("                    case 8:\n");
		sb.append("                        instance.dataNotifyType = (" + fd.getPackage()
				+ ".DataNotifyType)global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadUInt64(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                    // Field 2 LengthDelimited\n");
		sb.append("                    case 18:\n");
		sb.append(
				"                        instance.dataNotifyBytes = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadBytes(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                    // Field 3 LengthDelimited\n");
		sb.append("                    case 26:\n");
		sb.append(
				"                        instance.jsonDesc = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadString(stream);\n");
		sb.append("                        continue;\n");
		sb.append("                }\n");
		sb.append("\n");
		sb.append(
				"                var key = global::SilentOrbit.ProtocolBuffers.ProtocolParser.ReadKey((byte)keyByte, stream);\n");
		sb.append("\n");
		sb.append("                // Reading field ID > 16 and unknown field ID/wire type combinations\n");
		sb.append("                switch (key.Field)\n");
		sb.append("                {\n");
		sb.append("                    case 0:\n");
		sb.append(
				"                        throw new global::SilentOrbit.ProtocolBuffers.ProtocolBufferException(\"Invalid field id: 0, something went wrong in the stream\");\n");
		sb.append("                    default:\n");
		sb.append("                        global::SilentOrbit.ProtocolBuffers.ProtocolParser.SkipKey(stream, key);\n");
		sb.append("                        break;\n");
		sb.append("                }\n");
		sb.append("            }\n");
		sb.append("\n");
		sb.append("            return instance;\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append("        /// <summary>Serialize the instance into the stream</summary>\n");
		sb.append("        public static void Serialize(Stream stream, " + className + " instance)\n");
		sb.append("        {\n");
		sb.append("            var msField = global::SilentOrbit.ProtocolBuffers.ProtocolParser.Stack.Pop();\n");
		sb.append("            // Key for field: 1, Varint\n");
		sb.append("            stream.WriteByte(8);\n");
		sb.append(
				"            global::SilentOrbit.ProtocolBuffers.ProtocolParser.WriteUInt64(stream,(ulong)instance.dataNotifyType);\n");
		sb.append("            if (instance.dataNotifyBytes != null)\n");
		sb.append("            {\n");
		sb.append("                // Key for field: 2, LengthDelimited\n");
		sb.append("                stream.WriteByte(18);\n");
		sb.append(
				"                global::SilentOrbit.ProtocolBuffers.ProtocolParser.WriteBytes(stream, instance.dataNotifyBytes);\n");
		sb.append("            }\n");
		sb.append("            if (instance.jsonDesc != null)\n");
		sb.append("            {\n");
		sb.append("                // Key for field: 3, LengthDelimited\n");
		sb.append("                stream.WriteByte(26);\n");
		sb.append(
				"                global::SilentOrbit.ProtocolBuffers.ProtocolParser.WriteBytes(stream, Encoding.UTF8.GetBytes(instance.jsonDesc));\n");
		sb.append("            }\n");
		sb.append("            global::SilentOrbit.ProtocolBuffers.ProtocolParser.Stack.Push(msField);\n");
		sb.append("        }\n");
		sb.append("\n");
		sb.append("        /// <summary>Helper: Serialize into a MemoryStream and return its byte array</summary>\n");
		sb.append("        public static byte[] SerializeToBytes(" + className + " instance)\n");
		sb.append("        {\n");
		sb.append("            using (var ms = new MemoryStream())\n");
		sb.append("            {\n");
		sb.append("                Serialize(ms, instance);\n");
		sb.append("                return ms.ToArray();\n");
		sb.append("            }\n");
		sb.append("        }\n");
		sb.append("        /// <summary>Helper: Serialize with a varint length prefix</summary>\n");
		sb.append("        public static void SerializeLengthDelimited(Stream stream, " + className + " instance)\n");
		sb.append("        {\n");
		sb.append("            var data = SerializeToBytes(instance);\n");
		sb.append(
				"            global::SilentOrbit.ProtocolBuffers.ProtocolParser.WriteUInt32(stream, (uint)data.Length);\n");
		sb.append("            stream.Write(data, 0, data.Length);\n");
		sb.append("        }\n");
		sb.append("    }\n");

		return false;
	}

	public static byte[] readFileBytes(String pbFilePath) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(pbFilePath);
			byte[] bs = new byte[(int) new File(pbFilePath).length()];
			fis.read(bs);
			fis.close();
			return bs;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 获取序列化的tag
	 * 
	 * @param fieldNumber
	 * @param type
	 * @see https://developers.google.com/protocol-buffers/docs/encoding
	 * @return
	 */
	public static int getPbtag(int fieldNumber, com.google.protobuf.FieldDescriptorProto.Type type) {
		int tag = 0;
		int t = 0;
		if (type.equals(FieldDescriptorProto.Type.TYPE_FIXED64) || type.equals(FieldDescriptorProto.Type.TYPE_SFIXED64)
				|| type.equals(FieldDescriptorProto.Type.TYPE_DOUBLE)) {
			t = 1;
		}
		if (type.equals(FieldDescriptorProto.Type.TYPE_SFIXED32) || type.equals(FieldDescriptorProto.Type.TYPE_FIXED32)
				|| type.equals(FieldDescriptorProto.Type.TYPE_FLOAT)) {
			t = 5;
		}

		if (type.equals(FieldDescriptorProto.Type.TYPE_STRING) || type.equals(FieldDescriptorProto.Type.TYPE_BYTES)
				|| type.equals(FieldDescriptorProto.Type.TYPE_MESSAGE)) {
			t = 2;
		}
		tag = fieldNumber << 3 | t;
		return tag;
	}

	public static String getPbTypeStr(com.google.protobuf.FieldDescriptorProto.Type type) {
		int t = getPbType(type);
		if (t == 0) {
			return "Varint";
		}
		if (t == 1) {
			return "64-bit";
		}
		if (t == 2) {
			return "Length-delimited";
		}
		if (t == 5) {
			return "32-bit";
		}
		return "err";
	}

	public static int getPbType(com.google.protobuf.FieldDescriptorProto.Type type) {
		int t = 0;
		if (type.equals(FieldDescriptorProto.Type.TYPE_FIXED64) || type.equals(FieldDescriptorProto.Type.TYPE_SFIXED64)
				|| type.equals(FieldDescriptorProto.Type.TYPE_DOUBLE)) {
			t = 1;
		}
		if (type.equals(FieldDescriptorProto.Type.TYPE_SFIXED32) || type.equals(FieldDescriptorProto.Type.TYPE_FIXED32)
				|| type.equals(FieldDescriptorProto.Type.TYPE_FLOAT)) {
			t = 5;
		}

		if (type.equals(FieldDescriptorProto.Type.TYPE_STRING) || type.equals(FieldDescriptorProto.Type.TYPE_BYTES)
				|| type.equals(FieldDescriptorProto.Type.TYPE_MESSAGE)) {
			t = 2;
		}
		return t;
	}
}
