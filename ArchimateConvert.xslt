<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengroup.org/xsd/archimate http://www.opengroup.org/xsd/archimate/archimate_v2p1.xsd">
	<!-- standard copy template -->
	<xsl:template match="model|element|label|model/name|documentation|propertydef|view|node|fillColor|lineColor|connection|relationship|item/item">
		<xsl:element name="{name()}">
			<xsl:if test="identifier"><xsl:attribute name="identifier"><xsl:value-of select="identifier/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="propertydef/name"><xsl:attribute name="name"><xsl:value-of select="name/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="type"><xsl:attribute name="type"><xsl:value-of select="type/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="elementref"><xsl:attribute name="elementref"><xsl:value-of select="elementref/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="x"><xsl:attribute name="x"><xsl:value-of select="x/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="y"><xsl:attribute name="y"><xsl:value-of select="y/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="w"><xsl:attribute name="w"><xsl:value-of select="w/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="h"><xsl:attribute name="h"><xsl:value-of select="h/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="r"><xsl:attribute name="r"><xsl:value-of select="r/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="g"><xsl:attribute name="g"><xsl:value-of select="g/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="b"><xsl:attribute name="b"><xsl:value-of select="b/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="identifierref"><xsl:attribute name="identifierref"><xsl:value-of select="identifierref/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="source"><xsl:attribute name="source"><xsl:value-of select="source/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="relationshipref"><xsl:attribute name="relationshipref"><xsl:value-of select="relationshipref/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="target"><xsl:attribute name="target"><xsl:value-of select="target/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="xsi:type"><xsl:attribute name="xsi:type"><xsl:value-of select="xsi:type/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="xml:lang"><xsl:attribute name="xml:lang"><xsl:value-of select="xml:lang/text()"/></xsl:attribute></xsl:if>
			<xsl:if test="text()"><xsl:copy-of select="./text()" /></xsl:if>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>	
	<xsl:template match="identifier|identifierref|xsi:type|xml:lang|type|propertydef/name|x|y|w|h|elementref|r|g|b|source|target|relationshipref">
	</xsl:template>
	<xsl:template match="xml:lang">
	</xsl:template>
	<xsl:template match="@*">
		<xsl:copy>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="node()">
		<xsl:element name="{name()}">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>	
	
</xsl:stylesheet>
