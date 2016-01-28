<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns="http://www.opengroup.org/xsd/archimate" xmlns:arc="http://www.opengroup.org/xsd/archimate"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.opengroup.org/xsd/archimate http://www.opengroup.org/xsd/archimate/archimate_v2p1.xsd">
	<!-- standard copy template -->
	<xsl:output indent="yes" />
	<xsl:template
		match="arc:model|arc:element|arc:label|arc:name|arc:item|arc:value|arc:documentation|arc:property|arc:propertydef|arc:view|arc:node|arc:fillColor|arc:lineColor|arc:connection|arc:relationship">
		<!-- xsl:element name="{name()}" -->
		<xsl:copy>
			<xsl:if test="arc:identifier">
				<xsl:attribute name="identifier"><xsl:value-of
					select="arc:identifier/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:name[parent::arc:propertydef]">
				<xsl:attribute name="name"><xsl:value-of select="arc:name/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:type">
				<xsl:attribute name="type"><xsl:value-of select="arc:type/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:elementref">
				<xsl:attribute name="elementref"><xsl:value-of
					select="arc:elementref/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:x">
				<xsl:attribute name="x"><xsl:value-of select="arc:x/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:y">
				<xsl:attribute name="y"><xsl:value-of select="arc:y/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:w">
				<xsl:attribute name="w"><xsl:value-of select="arc:w/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:h">
				<xsl:attribute name="h"><xsl:value-of select="arc:h/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:r">
				<xsl:attribute name="r"><xsl:value-of select="arc:r/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:g">
				<xsl:attribute name="g"><xsl:value-of select="arc:g/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:b">
				<xsl:attribute name="b"><xsl:value-of select="arc:b/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:identifierref">
				<xsl:attribute name="identifierref"><xsl:value-of
					select="arc:identifierref/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:source">
				<xsl:attribute name="source"><xsl:value-of select="arc:source/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:relationshipref">
				<xsl:attribute name="relationshipref"><xsl:value-of
					select="arc:relationshipref/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="arc:target">
				<xsl:attribute name="target"><xsl:value-of select="arc:target/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="xsi:type">
				<xsl:attribute name="xsi:type"><xsl:value-of select="xsi:type/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="xml:lang">
				<xsl:attribute name="xml:lang"><xsl:value-of select="xml:lang/text()" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="text()">
				<xsl:copy-of select="./text()" />
			</xsl:if>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates select="*" />
		</xsl:copy>
		<!-- /xsl:element -->
	</xsl:template>
	<xsl:template
		match="arc:identifier|arc:identifierref|xsi:type|xml:lang|arc:type|arc:propertydef/arc:name|arc:x|arc:y|arc:w|arc:h|arc:elementref|arc:r|arc:g|arc:b|arc:source|arc:target|arc:relationshipref">
	</xsl:template>

	<xsl:template match="xml:lang">
	</xsl:template>

	<xsl:template match="@*">
		<xsl:copy />
	</xsl:template>

	<xsl:template match="*">
		<xsl:element name="{name()}">
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
