import json
import boto3
from PIL import Image
import tempfile
import os
import time

s3 = boto3.client("s3")
dynamodb = boto3.resource("dynamodb")
sns = boto3.client("sns")

TABLE_NAME = "ImageMetadata"
PROCESSED_BUCKET = "my-image-processed-first"
SNS_TOPIC_ARN = "arn:aws:sns:ap-northeast-1:796796207962:image-processed-topic"

table = dynamodb.Table(TABLE_NAME)

def lambda_handler(event, context):
    print("Event:", json.dumps(event))

    if "Records" not in event:
        print("No records found.")
        return {"status": "no records"}

    for record in event["Records"]:
        try:
            body = json.loads(record["body"])
            image_key = body["s3Key"]
            image_id = body["imageId"]
            original_bucket = body["bucket"]
        except Exception as e:
            print("❌ Error parsing SQS body:", e)
            continue

        try:
            with tempfile.TemporaryDirectory() as tmpdir:
                local_path = os.path.join(tmpdir, "input.jpg")
                s3.download_file(original_bucket, image_key, local_path)

                thumbnail_path = os.path.join(tmpdir, "thumb.jpg")

                with Image.open(local_path) as img:
                    img.thumbnail((300, 300))
                    img.save(thumbnail_path)

                processed_key = f"processed/{image_id}.jpg"
                s3.upload_file(thumbnail_path, PROCESSED_BUCKET, processed_key)

            table.put_item(
                Item={
                    "imageId": image_id,
                    "originalS3Key": image_key,
                    "processedS3Key": processed_key,
                    "status": "DONE",
                    "updatedAt": int(time.time())
                }
            )

            sns.publish(
                TopicArn=SNS_TOPIC_ARN,
                Subject="Image Processing Completed",
                Message=f"Image {image_id} has been processed and stored at {processed_key}"
            )

            print(f"Image {image_id} processed successfully.")

        except Exception as e:
            print(f"❌ Error processing image {image_id}: {e}")

    return {"status": "ok"}
