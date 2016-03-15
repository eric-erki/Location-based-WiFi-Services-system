<?php
/**
 * phpsnmp - a PHP SNMP library
 *
 * Copyright (C) 2004 David Eder <david@eder,us>
 *
 * Based on snmp - a Python SNMP library
 * Copyright (C) 2003 Unicity Pty Ltd <libsnmp@unicity.com.au>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author David Eder <david@eder.us>
 * @copyright 2004 David Eder
 * @package phpSNMP
 * @subpackage rfc1157
 * @version .7
 */

/**
 */
require_once(dirname(__FILE__) . DIRECTORY_SEPARATOR . 'rfc1155.php');

define('ASN_TAG_GET',                   0x00);
define('ASN_TAG_GETNEXT',               0x01);
define('ASN_TAG_RESPONSE',              0x02);
define('ASN_TAG_SET',                   0x03);
define('ASN_TAG_TRAP',                  0x04);

$ASN_TAG_DICT[0xa0] = 'rfc1157_Get';
$ASN_TAG_DICT[0xa1] = 'rfc1157_GetNext';
$ASN_TAG_DICT[0xa2] = 'rfc1157_Response';
$ASN_TAG_DICT[0xa3] = 'rfc1157_Set';
$ASN_TAG_DICT[0xa4] = 'rfc1157_TrapPDU';

/**
 * Error Status
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_ErrorStatus extends rfc1155_Integer
{
 /**
  * Constructor
  *
  * @param integer $value
  */
  function rfc1157_ErrorStatus($value)
  {
    parent::rfc1155_Integer($value);
  }

 /**
  * ToString
  *
  * @return string value of this object
  */
  function toString()
  {
    switch($this->value)
    {
      case 0: return 'No Error';
      case 1: return 'Response message would have been too large';
      case 2: return 'There is no such variable name in this MIB';
      case 3: return 'The value given has the wrong type';
      case 4: return 'Object is Read Only';
    }
    return 'An unknown error occurred';
  }
}

/**
 * Variable Binding
 *
 * This binds a name to an object
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_VarBind extends rfc1155_Sequence
{
 /**
  * Constructor
  *
  * @param rfc1155_ObjectID $name
  * @param rfc1155_Asn1Object $value
  */
  function rfc1157_VarBind($name=NULL, $value=NULL)
  {
    if(is_null($name)) $name = new rfc1155_ObjectID();
    if(is_null($value)) $value = new rfc1155_Null();
    if(is_string($name)) $name = new rfc1155_ObjectID($name);

    if(!is_a($name, 'rfc1155_ObjectID'))
      trigger_error('name must be an rfc1155_ObjectID', E_USER_WARNING);
    if(!is_a($value, 'rfc1155_Asn1Object'))
      trigger_error('value must be an rfc1155_Asn1Object', E_USER_WARNING);
    parent::rfc1155_Sequence(array($name, $value));
  }

 /**
  * Get/Set oid
  *
  * @param rfc1155_ObjectID $value
  * @return rfc1155_ObjectID
  */
  function oid($value=NULL)
  {
    if(!is_null($value))
    {
      if(is_a($value, rfc1155_ObjectID))
        $this->value[0] = $value;
      else
        $this->value[0] = new rfc1155_ObjectID($value);
    }
    return $this->value[0];
  }

 /**
  * Get/Set value
  *
  * @param rfc1155_Asn1Object $value
  * @return rfc1155_Asn1Object
  */
  function value($value=NULL)
  {
    if(!is_null($value)) $this->value[1] = $value;
    return $this->value[1];
  }
}

/**
 * Variable Binding List
 *
 * A Sequence of VarBinds
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_VarBindList extends rfc1155_SequenceOf
{
 /**
  * Constructor
  *
  * @param array $value of rfc1157_VarBind
  */
  function rfc1157_VarBindList($value=array())
  {
    parent::rfc1155_SequenceOf('rfc1157_VarBind', $value);
  }
}

/**
 * Message
 *
 * A Message is the base comms type for all SNMP messages
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_Message extends rfc1155_Sequence
{
 /**
  * Constructor
  *
  * @param integer $version
  * @param string $community
  * @param rfc1157_PDU $pdu
  */
  function rfc1157_Message($version=0, $community='public', $pdu=NULL)
  {
    parent::rfc1155_Sequence();
    if(is_null($pdu)) $pdu = new rfc1157_PDU();
    $this->value = array(new rfc1155_Integer($version), new rfc1155_OctetString($community), $pdu);
  }

 /**
  * Get/Set version
  *
  * @param integer $value
  * @return integer
  */
  function version($value=NULL)
  {
    if(!is_null($value)) $this->value[0] = new rfc1155_Integer($value);
    return $this->value[0]->value;
  }

 /**
  * Get/Set community
  *
  * @param string $value
  * @return string
  */
  function community($value=NULL)
  {
    if(!is_null($value)) $this->value[1] = new rfc1155_OctetString($value);
    return $this->value[1]->value;
  }

 /**
  * Get/Set PDU
  *
  * @param rfc1157_PDU $value
  * @return rfc1157_PDU
  */
  function pdu($value=NULL)
  {
    if(!is_null($value)) $this->value[2] = $value;
    return $this->value[2];
  }

 /**
  * Decode Stream
  *
  * decode() an octet stream into a sequence of Asn1Objects
  *
  * @param string $stream
  * @return rfc1157_Message
  */
  function decode($stream)
  {
    $this->value = parent::decode($stream);
    if(count($this->value) != 1)
      trigger_error('Malformed Message: More than one object decoded.', E_USER_WARNING);
    $this->value = $this->value[0]->value;
    if(count($this->value) != 3)
      trigger_error('Malformed Message: Incorrect sequence length ' . count($this->value[0]->value), E_USER_WARNING);
    return $this;
  }
}

/**
 * PDU
 *
 * Base clss for a non-trap PDU
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_PDU extends rfc1155_Sequence // Base class for a non-trap PDU
{
 /**
  * Constructor
  *
  * @param integer $requestID
  * @param integer $errorStatus
  * @param integer $errorIndex
  * @param array $varBindList
  */
  function rfc1157_PDU($requestID=0, $errorStatus=0, $errorIndex=0, $varBindList=array())
  {
    /* this allows you to create a new object with no arguments, arguments of the class ultimately desired (eg Integer)
       or, to make like easier, it will convert basic strings and ints into the ultimately desired objects. */

    parent::rfc1155_Sequence();
    $this->asnTagClass = ASN_TAG_CLASS_CONTEXT;
    $this->value = array(new rfc1155_Integer($requestID), new rfc1157_ErrorStatus($errorStatus),
                         new rfc1155_Integer($errorIndex), new rfc1157_VarBindList($varBindList));
  }

 /**
  * Get/Set Request ID
  *
  * @param integer $value
  * @return integer
  */
  function requestID($value=NULL)
  {
    if(!is_null($value)) $this->value[0] = new rfc1155_Integer($value);
    return $this->value[0]->value;
  }

 /**
  * Get/Set Error Status
  *
  * @param integer $value
  * @return integer
  */
  function errorStatus($value=NULL)
  {
    if(!is_null($value)) $this->value[1] = new rfc1157_ErrorStatus($value);
    return $this->value[1]->value;
  }

 /**
  * Get Error String
  *
  * @return string
  */
  function errorString()
  {
    return $this->value[1]->toString();
  }

 /**
  * Get/Set Error Index
  *
  * @param integer $value
  * @return integer
  */
  function errorIndex($value=NULL)
  {
    if(!is_null($value)) $this->value[2] = new rfc1155_Integer($value);
    return $this->value[2]->value;
  }

 /**
  * Get/Set Var Bind List
  *
  * @param rfc1157_VarBindList $value
  * @return rfc1157_VarBindList
  */
  function varBindList($value=NULL)
  {
    if(!is_null($value)) $this->value[3] = new rfc1157_VarBindList($value);
    return $this->value[3]->value;
  }

 /**
  * Decode into a PDU Object
  *
  * @param string $stream
  * @return rfc1157_PDU
  */
  function decodeContents($stream)
  {
    parent::decodeContents($stream);
    if(count($this->value) != 4)
      trigger_error('Malformed PDU: Incorrect length ' . count($this->value), E_USER_WARNING);

    $this->value[1] = new rfc1157_ErrorStatus($this->value[1]->value);
    $this->value[3] = new rfc1157_VarBindList($this->value[3]->value);
    return $this;
  }
}

/**
 * GET request
 *
 * A Get Request PDU
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_Get extends rfc1157_PDU
{
 /**
  * Constructor
  *
  * @param integer $requestID
  * @param integer $errorStatus
  * @param integer $errorIndex
  * @param array $varBindList
  */
  function rfc1157_Get($requestID=0, $errorStatus=0, $errorIndex=0, $varBindList=array())
  {
    parent::rfc1157_PDU($requestID, $errorStatus, $errorIndex, $varBindList);
    $this->asnTagNumber = ASN_TAG_GET;
  }
}

/**
 * GETNEXT request
 *
 * A GetNext Request PDU
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_GetNext extends rfc1157_PDU
{
 /**
  * Constructor
  *
  * @param integer $requestID
  * @param integer $errorStatus
  * @param integer $errorIndex
  * @param array $varBindList
  */
  function rfc1157_GetNext($requestID=0, $errorStatus=0, $errorIndex=0, $varBindList=array())
  {
    parent::rfc1157_PDU($requestID, $errorStatus, $errorIndex, $varBindList);
    $this->asnTagNumber = ASN_TAG_GETNEXT;
  }
}

/**
 * RESPONSE request
 *
 * A Response PDU
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_Response extends rfc1157_PDU
{
 /**
  * Constructor
  *
  * @param integer $requestID
  * @param integer $errorStatus
  * @param integer $errorIndex
  * @param array $varBindList
  */
  function rfc1157_Response($requestID=0, $errorStatus=0, $errorIndex=0, $varBindList=array())
  {
    parent::rfc1157_PDU($requestID, $errorStatus, $errorIndex, $varBindList);
    $this->asnTagNumber = ASN_TAG_GET;
  }
}

/**
 * SET request
 *
 * A Set Request PDU
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_Set extends rfc1157_PDU
{
 /**
  * Constructor
  *
  * @param integer $requestID
  * @param integer $errorStatus
  * @param integer $errorIndex
  * @param array $varBindList
  */
  function rfc1157_Set($requestID=0, $errorStatus=0, $errorIndex=0, $varBindList=array())
  {
    parent::rfc1157_PDU($requestID, $errorStatus, $errorIndex, $varBindList);
    $this->asnTagNumber = ASN_TAG_SET;
  }
}

define('TRAP_COLDSTART', 0);
define('TRAP_WARMSTART', 1);
define('TRAP_LINKDOWN', 2);
define('TRAP_LINKUP', 3);
define('TRAP_AUTH_FAIL', 4);
define('TRAP_EGP_NEIGHBOR_LOSS', 5);
define('TRAP_ENTERPRISE_SPECIFIC', 6);

/**
 * Generic Trap
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_GenericTrap extends rfc1155_Integer
{
  var $genericTraps;

 /**
  * Constructor
  *
  * @param integer $value
  */
  function rfc1157_GenericTrap($value)
  {
    parent::rfc1155_Integer($value);
  }
}

/**
 * Trap PDU
 *
 * @package phpSNMP
 * @subpackage rfc1157
 */
class rfc1157_TrapPDU extends rfc1155_Sequence
{
 /**
  * Constructor
  *
  * @param string $enterprise
  * @param string $agentAddr
  * @param integer $genericTrap
  * @param integer $specificTrap
  * @param integer $timestamp
  * @param array $varBindList
  */
  function rfc1157_TrapPDU($enterprise=NULL, $agentAddr=NULL, $genericTrap=NULL, $specificTrap=NULL, $timestamp=NULL, $varBindList=array())
  {
    parent::rfc1155_Sequence();
    $this->asnTagClass = ASN_TAG_CLASS_CONTEXT;
    $this->asnTagNumber = ASN_TAG_TRAP;
    $this->value = array(new rfc1155_ObjectID($enterprise), new rfc1155_NetworkAddress($agentAddr),
                         new rfc1157_GenericTrap($genericTrap), new rfc1155_Integer($specificTrap),
                         new rfc1155_TimeTicks($timestamp), new rfc1157_VarBindList($varBindList));
  }

 /**
  * Get/Set Enterprise OID
  *
  * @param string $value
  * @return string
  */
  function enterprise($value=NULL)
  {
    if(!is_null($value)) $this->value[0] = new rfc1155_ObjectID($value);
    return $this->value[0]->toString();
  }

 /**
  * Get/Set Agent Address
  *
  * @param string $value
  * @return string
  */
  function agentAddr($value=NULL)
  {
    if(!is_null($value)) $this->value[1] = new rfc1155_NetworkAddress($value);
    return $this->value[1]->value;
  }

 /**
  * Get/Set Generic Trap
  *
  * @param integer $value
  * @return integer
  */
  function genericTrap($value=NULL)
  {
    if(!is_null($value)) $this->value[2]->value = $value;
    return $this->value[2]->value;
  }

 /**
  * Get/Set Specific Trap
  *
  * @param integer $value
  * @return integer
  */
  function specificTrap($value=NULL)
  {
    if(!is_null($value)) $this->value[3]->value = $value;
    return $this->value[3]->value;
  }

 /**
  * Get/Set Timestamp
  *
  * @param integer $value
  * @return integer
  */
  function timestamp($value=NULL)
  {
    if(!is_null($value)) $this->value[4]->value = $value;
    return $this->value[4]->value;
  }

 /**
  * Get/Set Var Bind List
  *
  * @param rfc1157_VarBindList $value
  * @return rfc1157_VarBindList
  */
  function VarBindList($value=NULL)
  {
    if(!is_null($value)) $this->value[5] = $value;
    return $this->value[5];
  }

 /**
  * Decode into a Get PDU Object
  *
  * @param string $stream
  * @return rfc1157_TrapPDU
  */
  function decodeContents($stream)
  {
    parent::decodeContents($stream);
    if(count($this->value) != 6)
      trigger_error('Malformed TrapPDU: Incorrect length ' . count($this->value), E_USER_WARNING);

    $this->value[1] = new rfc1155_NetworkAddress($this->value[1]->value);
    $this->value[5] = new rfc1157_VarBindList($this->value[5]->value);

    return $this;
  }
}
?>
