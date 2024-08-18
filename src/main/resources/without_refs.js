const spec ={
  "openapi": "3.0.1",
  "info": {
    "version": "1.0.0",
    "title": "API Specification Example"
  },
  "paths": {
    "/articles": {
      "post": {
        "summary": "Create an article.",
        "operationId": "createArticle",
        "tags": ["Article API"],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "allOf": [
                  {
                    "properties": {
                      "id": {
                        "description": "Resource ID",
                        "type": "integer",
                        "format": "int64",
                        "readOnly": true,
                        "example": 1
                      },
                      "category": {
                        "description": "Category of an article",
                        "type": "string",
                        "example": "sports"
                      }
                    }
                  },
                  {
                    "required": ["text"],
                    "properties": {
                      "text": {
                        "description": "Content of an article",
                        "type": "string",
                        "maxLength": 1024,
                        "example": "# Title\n\n## Head Line\n\nBody"
                      }
                    }
                  }
                ]
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Success",
            "content": {
              "application/json": {
                "schema": {
                  "allOf": [
                    {
                      "properties": {
                        "id": {
                          "description": "Resource ID",
                          "type": "integer",
                          "format": "int64",
                          "readOnly": true,
                          "example": 1
                        },
                        "category": {
                          "description": "Category of an article",
                          "type": "string",
                          "example": "sports"
                        }
                      }
                    },
                    {
                      "required": ["text"],
                      "properties": {
                        "text": {
                          "description": "Content of an article",
                          "type": "string",
                          "maxLength": 1024,
                          "example": "# Title\n\n## Head Line\n\nBody"
                        }
                      }
                    }
                  ]
                }
              }
            }
          },
          "400": {
            "description": "The input is invalid.",
            "content": {
              "application/json": {
                "schema": {
                  "description": "<table>\n  <tr>\n    <th>Code</th>\n    <th>Description</th>\n  </tr>\n  <tr>\n    <td>illegal_input</td>\n    <td>The input is invalid.</td>\n  </tr>\n  <tr>\n    <td>not_found</td>\n    <td>The resource is not found.</td>\n  </tr>\n</table>\n",
                  "required": ["code", "message"],
                  "properties": {
                    "code": {
                      "type": "string",
                      "example": "illegal_input"
                    }
                  }
                }
              }
            }
          }
        }
      },
      "get": {
        "summary": "Get a list of articles",
        "operationId": "listArticles",
        "tags": ["Article API"],
        "parameters": [
          {
            "name": "limit",
            "in": "query",
            "description": "limit",
            "required": false,
            "schema": {
              "type": "integer",
              "minimum": 1,
              "maximum": 100,
              "default": 10,
              "example": 10
            }
          },
          {
            "name": "offset",
            "in": "query",
            "description": "offset",
            "required": false,
            "schema": {
              "type": "integer",
              "minimum": 0,
              "default": 0,
              "example": 10
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": {
                    "properties": {
                      "id": {
                        "description": "Resource ID",
                        "type": "integer",
                        "format": "int64",
                        "readOnly": true,
                        "example": 1
                      },
                      "category": {
                        "description": "Category of an article",
                        "type": "string",
                        "example": "sports"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    "/articles/{id}": {
      "get": {
        "summary": "Get an article.",
        "operationId": "getArticle",
        "tags": ["Article API"],
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "Resource ID",
            "required": true,
            "schema": {
              "description": "Resource ID",
              "type": "integer",
              "format": "int64",
              "readOnly": true,
              "example": 1
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "content": {
              "application/json": {
                "schema": {
                  "allOf": [
                    {
                      "properties": {
                        "id": {
                          "description": "Resource ID",
                          "type": "integer",
                          "format": "int64",
                          "readOnly": true,
                          "example": 1
                        },
                        "category": {
                          "description": "Category of an article",
                          "type": "string",
                          "example": "sports"
                        }
                      }
                    },
                    {
                      "required": ["text"],
                      "properties": {
                        "text": {
                          "description": "Content of an article",
                          "type": "string",
                          "maxLength": 1024,
                          "example": "# Title\n\n## Head Line\n\nBody"
                        }
                      }
                    }
                  ]
                }
              }
            }
          },
          "404": {
            "description": "The resource is not found.",
            "content": {
              "application/json": {
                "schema": {
                  "description": "<table>\n  <tr>\n    <th>Code</th>\n    <th>Description</th>\n  </tr>\n  <tr>\n    <td>illegal_input</td>\n    <td>The input is invalid.</td>\n  </tr>\n  <tr>\n    <td>not_found</td>\n    <td>The resource is not found.</td>\n  </tr>\n</table>\n",
                  "required": ["code", "message"],
                  "properties": {
                    "code": {
                      "type": "string",
                      "example": "not_found"
                    }
                  }
                }
              }
            }
          }
        }
      },
      "put": {
        "summary": "Update",
        "operationId": "updateArticle",
        "tags": ["Article API"],
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "Resource ID",
            "required": true,
            "schema": {
              "description": "Resource ID",
              "type": "integer",
              "format": "int64",
              "readOnly": true,
              "example": 1
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "allOf": [
                  {
                    "properties": {
                      "id": {
                        "description": "Resource ID",
                        "type": "integer",
                        "format": "int64",
                        "readOnly": true,
                        "example": 1
                      },
                      "category": {
                        "description": "Category of an article",
                        "type": "string",
                        "example": "sports"
                      }
                    }
                  },
                  {
                    "required": ["text"],
                    "properties": {
                      "text": {
                        "description": "Content of an article",
                        "type": "string",
                        "maxLength": 1024,
                        "example": "# Title\n\n## Head Line\n\nBody"
                      }
                    }
                  }
                ]
              }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Success",
            "content": {
              "application/json": {
                "schema": {
                  "allOf": [
                    {
                      "properties": {
                        "id": {
                          "description": "Resource ID",
                          "type": "integer",
                          "format": "int64",
                          "readOnly": true,
                          "example": 1
                        },
                        "category": {
                          "description": "Category of an article",
                          "type": "string",
                          "example": "sports"
                        }
                      }
                    },
                    {
                      "required": ["text"],
                      "properties": {
                        "text": {
                          "description": "Content of an article",
                          "type": "string",
                          "maxLength": 1024,
                          "example": "# Title\n\n## Head Line\n\nBody"
                        }
                      }
                    }
                  ]
                }
              }
            }
          },
          "404": {
            "description": "The resource is not found.",
            "content": {
              "application/json": {
                "schema": {
                  "description": "<table>\n  <tr>\n    <th>Code</th>\n    <th>Description</th>\n  </tr>\n  <tr>\n    <td>illegal_input</td>\n    <td>The input is invalid.</td>\n  </tr>\n  <tr>\n    <td>not_found</td>\n    <td>The resource is not found.</td>\n  </tr>\n</table>\n",
                  "required": ["code", "message"],
                  "properties": {
                    "code": {
                      "type": "string",
                      "example": "not_found"
                    }
                  }
                }
              }
            }
          }
        }
      },
      "delete": {
        "summary": "Delete an article.",
        "operationId": "deleteArticle",
        "tags": ["Article API"],
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "Resource ID",
            "required": true,
            "schema": {
              "description": "Resource ID",
              "type": "integer",
              "format": "int64",
              "readOnly": true,
              "example": 1
            }
          }
        ],
        "responses": {
          "204": {
            "description": "Success"
          },
          "404": {
            "description": "The resource is not found.",
            "content": {
              "application/json": {
                "schema": {
                  "description": "<table>\n  <tr>\n    <th>Code</th>\n    <th>Description</th>\n  </tr>\n  <tr>\n    <td>illegal_input</td>\n    <td>The input is invalid.</td>\n  </tr>\n  <tr>\n    <td>not_found</td>\n    <td>The resource is not found.</td>\n  </tr>\n</table>\n",
                  "required": ["code", "message"],
                  "properties": {
                    "code": {
                      "type": "string",
                      "example": "not_found"
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

